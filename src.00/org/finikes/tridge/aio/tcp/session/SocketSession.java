package org.finikes.tridge.aio.tcp.session;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.finikes.tridge.aio.tcp.writer.TcpAsynWriter;
import org.finikes.tridge.aio.tcp.writer.TcpWriter;
import org.finikes.tridge.aio.tcp.writer.WriteObject;
import org.finikes.tridge.aio.tcp.writer.WriterPool;
import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecFactory;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;
import org.finikes.tridge.tcp.processor.Processor;
import org.finikes.utils.array.FinikesBytes;

public class SocketSession {
	public static final String MSG_SURPLUS_LENGTH = "MSG_SURPLUS_LENGTH";
	public static final String MSG_TMP = "MSG_TMP";

	private static final TcpWriter writer0 = TcpWriter.getWriter();
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
	private final TcpAsynWriter writer;
	private volatile boolean close;
	private final String describe;
	private boolean futureClose;
	private AtomicInteger responseSize = new AtomicInteger();
	private Object client;
	private Object mergeSpace;
	private Object pushMergeSpace;
	private boolean lock;
	private final LinkedBlockingQueue<WriteObject> writtenStock = new LinkedBlockingQueue<WriteObject>(
			64);
	private boolean writeLock;

	public boolean isLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
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
		this.writer = WriterPool.getWriter();
		this.describe = "local:" + channel.getLocalAddress() + " remote:"
				+ remoteAddress + " hashCode/" + this.hashCode();
		attachment = new ConcurrentHashMap<Object, Object>(128);
		this.attachment.put(MSG_SURPLUS_LENGTH, -1);
		this.attachment.put(MSG_TMP, new byte[0]);
		this.remotePort = Integer.parseInt(this.toString().split(" ")[1]
				.split(":")[2]);
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
		this.channel.write(buf);
		//writeBuffer(buf, response);
	}
	
	public void writeBuffer(ByteBuffer buffer, Object response) {
		this.addResponseSize();
		WriteObject wo = new WriteObject(this, buffer, response);
		synchronized (this) {
			if (writeLock) {
				this.writtenStock.offer(wo);
			}else

			writer0.write(wo, this);
		}
	}

	public void asynWrite(Object response) {
		this.addResponseSize();
		ByteBuffer buf = this.getCodec().encode(response, this);
		WriteObject writeObject = new WriteObject(this, buf, response);
		this.writer.getQueue().offer(writeObject);
	}

	public boolean close(boolean futureClose) {
		this.futureClose = true;

		/** 取消底层的心跳检测 
		 * if (TcpProactor.IS_OPEN_IDLE_DAEMON) {
			SessionManager.getInstance().removeSession(this);
		}*/

		if (futureClose) {
			if (responseSize.intValue() > 0) {
				return false;
			}
		}

		boolean isClosed = false;
		if (this.getChannel() != null && !isClose()) {
			try {
				this.getChannel().close();
				close = true;
				isClosed = true;
				IoHandlerFactory.getInstance().channelClose(this);
			} catch (Exception ignored) {
				ignored.printStackTrace();
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

	public AtomicInteger getResponseSize() {
		return responseSize;
	}

	public void addResponseSize() {
		responseSize.incrementAndGet();
	}

	public void reduceResponseSize() {
		responseSize.decrementAndGet();
		if (futureClose) {
			close(true);
		}
	}

	public Object getMergeSpace() {
		return mergeSpace;
	}

	public void setMergeSpace(Object mergeSpace) {
		this.mergeSpace = mergeSpace;
	}

	public boolean isWriteLock() {
		return writeLock;
	}

	public void setWriteLock(boolean writeLock) {
		this.writeLock = writeLock;
	}

	public LinkedBlockingQueue<WriteObject> getWrittenStock() {
		return writtenStock;
	}

	public TcpAsynWriter getWriter() {
		return writer;
	}

	public Object getPushMergeSpace() {
		return pushMergeSpace;
	}

	public void setPushMergeSpace(Object pushMergeSpace) {
		this.pushMergeSpace = pushMergeSpace;
	}
}
