package org.finikes.tridge.aio.tcp.session;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.finikes.WrittenQueue;
import org.finikes.tridge.aio.tcp.TcpProactor;
import org.finikes.tridge.aio.tcp.writer.AsyncWriter;
import org.finikes.tridge.aio.tcp.writer.WriteObject;
import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecFactory;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;
import org.finikes.tridge.tcp.processor.Processor;
import org.finikes.utils.array.FinikesBytes;

public class SocketSession {
	public static String MSG_SURPLUS_LENGTH = "MSG_SURPLUS_LENGTH";
	public static String MSG_TMP = "MSG_TMP";

	private final Map<Object, Object> attachment;
	private final AsynchronousSocketChannel channel;
	private final SocketAddress remoteAddress;
	private final int remotePort;
	private long lastOptTime;
	private long lastReadTime;
	private long lastWriteTime;
	private final Processor processor;
	private final ByteBuffer buffer;
	private final Codec codec;
	private volatile boolean close;
	private final String describe;
	private boolean futureClose;
	private Object client;
	private Object mergeSpace;
	private Object pushMergeSpace;
	private final Queue<WriteObject> WRITTEN_QUEUE = new WrittenQueue<WriteObject>();
	private final int hashCode;
	private final AtomicBoolean pendingWrite = new AtomicBoolean();

	public AtomicBoolean getPendingWrite() {
		return pendingWrite;
	}

	public Object getClient() {
		return client;
	}

	public void setClient(Object client) {
		this.client = client;
	}

	// 清空消息暂存器
	public void clearMsgTmp() {
		this.attachment.put(MSG_TMP, new byte[0]);
		this.attachment.put(MSG_SURPLUS_LENGTH, -1);
	}

	// 递减剩余消息长度
	public void reduceMsgSurplusLength() {
		this.attachment.put(MSG_SURPLUS_LENGTH, this.getMsgSurplusLength()
				- this.buffer.capacity());
	}

	// 获取剩余消息长度
	public int getMsgSurplusLength() {
		return (Integer) this.getAttachment(MSG_SURPLUS_LENGTH);
	}

	// 获得消息暂存器内的消息
	public byte[] getMsgTmp() {
		return (byte[]) this.getAttachment(MSG_TMP);
	}

	// 给消息暂存器增加数据
	public void addMsgToMsgTmp(byte[] msgPart) {
		byte[] msgTmp = FinikesBytes.sysJoint(getMsgTmp(), msgPart);

		this.setAttachment(MSG_TMP, msgTmp);
	}

	public boolean isClose() {
		return close;
	}

	public void setClose(boolean close) {
		this.close = close;
	}

	public SocketSession(AsynchronousSocketChannel channel, ByteBuffer buffer,
			CodecFactory codecFactory) throws IOException {
		this.channel = channel;
		this.remoteAddress = channel.getRemoteAddress();
		this.processor = new Processor(this);
		this.buffer = buffer;
		this.codec = codecFactory.getCodec();
		this.describe = "local:" + channel.getLocalAddress() + " remote:"
				+ remoteAddress + " hashCode/" + super.hashCode();
		this.hashCode = super.hashCode();
		attachment = new ConcurrentHashMap<Object, Object>(128);
		this.attachment.put(MSG_SURPLUS_LENGTH, -1);
		this.attachment.put(MSG_TMP, new byte[0]);
		this.remotePort = Integer.parseInt(this.toString().split(" ")[1]
				.split(":")[2]);
	}

	public int hashCode() {
		return this.hashCode;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public Object getAttachment(Object key) {
		return attachment.get(key);
	}

	public void setAttachment(Object key, Object attachment) {
		this.attachment.put(key, attachment);
	}

	public AsynchronousSocketChannel getChannel() {
		return channel;
	}

	public void write(Object response) {
		ByteBuffer buf = this.getCodec().encode(response, this);
		this.writeBuffer(buf, response);//????????
		// this.channel.write(buf);
	}

	public void writeBuffer0(ByteBuffer buffer, Object response) {
		WriteObject wo = new WriteObject(this, buffer, response);
		boolean promptWrite = false;// 立即写
		synchronized (WRITTEN_QUEUE) {
			promptWrite = WRITTEN_QUEUE.isEmpty();
			WRITTEN_QUEUE.offer(wo);
		}
		if (promptWrite) {
			this.pendingWrite(wo,false);
		}
	}
	
	public void writeBufferK(ByteBuffer buffer, Object response) {
		WriteObject wo = new WriteObject(this, buffer, response);

		//AtomicBoolean promptWrite = new AtomicBoolean(true);
		if (pendingWrite.compareAndSet(WRITTEN_QUEUE.isEmpty(), false)) {
			WRITTEN_QUEUE.offer(wo);
			this.pendingWrite(wo,false);
			System.err.println("---------------------------------");
			return;
		}

		System.err.println("==================================");
		WRITTEN_QUEUE.offer(wo);
	}
	
	public void writeBuffer(ByteBuffer buffer, Object response) {
		WriteObject wo = new WriteObject(this, buffer, response);

		WRITTEN_QUEUE.offer(wo);
		
		pendingWrite(wo, false);
	}
	
	private static AsyncWriter WRITER = AsyncWriter.getInstance();
	
	public final void pendingWrite(WriteObject wo, boolean qiangzhi) {
		if (qiangzhi) {
			if (!isClose() && channel.isOpen()) {
				channel.write(wo.getResponse(), 5L, TimeUnit.SECONDS, this,
						WRITER);
			}
		}
		
		if (this.pendingWrite.compareAndSet(false, true)) {
			if (!isClose() && channel.isOpen()) {
				channel.write(wo.getResponse(), 5L, TimeUnit.SECONDS, this,
						WRITER);
			}
		}
	}
	
	public void writeBufferX(ByteBuffer buffer, Object response) {
		WriteObject wo = new WriteObject(this, buffer, response);

		if (pendingWrite.compareAndSet(false, true)) {
			WRITTEN_QUEUE.offer(wo);
			this.pendingWrite(wo,false);
			
			return;
		}
		
		WRITTEN_QUEUE.offer(wo);
	}

	@SuppressWarnings("deprecation")
	public boolean close(boolean futureClose) {
		this.futureClose = true;

		if (TcpProactor.IS_OPEN_IDLE_DAEMON) {
			SessionManager.getInstance().removeSession(this);
		}

		if (isClose()) {
			return true;
		}

		if (futureClose) {
			if (!WRITTEN_QUEUE.isEmpty()) {
				return false;
			}
		}

		boolean isClosed = false;
		if (this.getChannel() != null && !isClose()) {
			try {
				if (this.channel.isOpen()) {
					this.getChannel().close();
				}
				close = true;
				isClosed = true;
				IoHandlerFactory.getInstance().channelClose(this);
			} catch (Exception ignored) {
				IoHandlerFactory.getInstance().exceptionCaught(this, ignored);
			}

			return isClosed;
		}

		close = true;

		return true;
	}

	public long getLastOptTime() {
		return lastOptTime;
	}

	public void setLastOptTime(long lastOptTime) {
		this.lastOptTime = lastOptTime;
	}

	public long getLastReadTime() {
		return lastReadTime;
	}

	public void setLastReadTime(long lastReadTime) {
		this.lastReadTime = lastReadTime;
	}

	public long getLastWriteTime() {
		return lastWriteTime;
	}

	public void setLastWriteTime(long lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}

	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public Processor getProcessor() {
		return processor;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public Codec getCodec() {
		return codec;
	}

	public String toString() {
		return describe;
	}

	public boolean isFutureClose() {
		return futureClose;
	}

	public void setFutureClose(boolean futureClose) {
		this.futureClose = futureClose;
	}

	public Object getMergeSpace() {
		return mergeSpace;
	}

	public void setMergeSpace(Object mergeSpace) {
		this.mergeSpace = mergeSpace;
	}

	public Queue<WriteObject> getWrittenQueue() {
		return WRITTEN_QUEUE;
	}

	public Object getPushMergeSpace() {
		return pushMergeSpace;
	}

	public void setPushMergeSpace(Object pushMergeSpace) {
		this.pushMergeSpace = pushMergeSpace;
	}

	private boolean writeLock;

	public boolean isWriteLock() {
		return writeLock;
	}

	public void setWriteLock(boolean writeLock) {
		this.writeLock = writeLock;
	}
}
