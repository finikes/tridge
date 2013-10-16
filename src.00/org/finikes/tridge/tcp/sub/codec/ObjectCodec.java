package org.finikes.tridge.tcp.sub.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecException;
import org.finikes.utils.array.FinikesBytes;

public class ObjectCodec implements Codec {

	// 如果一条消息长度超过buffer容量怎么办?
	// 判断消息长度是否超了
	// 将所有消息数据(实际是消息的前一部分)[msgTmp]取出來放入session
	// 将剩余长度[msgSurplusLength]也放入session
	// 剩下的消息部分需要几个buffer[msgSurplusPart]也存放到session
	// 循环开始
	// 这一波能不能完全传输?(msgSurplusPart是否为0)
	// Y:
	// 从buffer0号位开始取出msgSurplusLength个字节[msgSurplus],msgSurplusPart和msgSurplusLength清零,结合msgSurplus、msgTmp生成对象,清空msgTmp循环结束
	// N:
	// 从buffer0号位开始取出所有字节,连接到msgTmp,更新消息长度(实际就是原msgSurplusLength-bufferCapacity),msgSurplusPart递减

	public List<Object> decode(SocketSession session) {
		ByteBuffer buffer = session.getBuffer();

		int bufferCapacity = buffer.capacity();
		List<Object> requests = new ArrayList<Object>();

		buffer.flip();
		for (;;) {
			int msgSurplusLength = session.getMsgSurplusLength();

			if (msgSurplusLength <= 0 && buffer.remaining() <= 4) {
				break;
			}

			if (msgSurplusLength > 0) {
				if (msgSurplusLength > bufferCapacity) {
					byte[] msgPartBytes = new byte[bufferCapacity];
					buffer.get(msgPartBytes, 0, bufferCapacity);

					session.addMsgToMsgTmp(msgPartBytes);

					session.reduceMsgSurplusLength();

					buffer.clear();

					return requests;
				} else {
					byte[] msgPartBytes = new byte[msgSurplusLength];
					buffer.get(msgPartBytes, 0, msgSurplusLength);

					byte[] msgTmp = session.getMsgTmp();
					msgTmp = FinikesBytes.sysJoint(msgTmp, msgPartBytes);

					session.clearMsgTmp();

					ByteArrayInputStream bis = new ByteArrayInputStream(msgTmp);
					ObjectInputStream ois = null;
					try {
						ois = new ObjectInputStream(bis);
						requests.add(ois.readObject());
						ois.close();
						bis.close();
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						continue;
					}

					if (buffer.hasRemaining()) {
						byte[] surplus = new byte[buffer.remaining()];
						buffer.get(surplus, 0, buffer.remaining());
						buffer.clear();
						buffer.put(surplus);
					} else {
						buffer.clear();
					}

					return requests;
				}
			}

			byte[] msgLengthBytes = new byte[4];
			buffer.get(msgLengthBytes, 0, 4);

			int msgLength = FinikesBytes.byteToInt(msgLengthBytes);

			if (msgLength > bufferCapacity - 4) {
				msgSurplusLength = msgLength - bufferCapacity + 4;
				session.setAttachment(SocketSession.MSG_SURPLUS_LENGTH,
						msgSurplusLength);
				byte[] msgTmp = new byte[buffer.remaining()];
				buffer.get(msgTmp, 0, buffer.remaining());
				session.addMsgToMsgTmp(msgTmp);
				break;
			}

			if (buffer.remaining() >= msgLength) {
				byte[] message = new byte[msgLength];
				buffer.get(message, 0, msgLength);

				// 这些字节转换成对象并送入列表
				ByteArrayInputStream bis = new ByteArrayInputStream(message);
				ObjectInputStream ois = null;
				try {
					ois = new ObjectInputStream(bis);
					requests.add(ois.readObject());
					ois.close();
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					continue;
				}
			} else {
				buffer.position(buffer.position() - 4);
				break;
			}
		}

		int remaining = buffer.remaining();
		if (buffer.hasRemaining()) {
			byte[] surplus = new byte[remaining];
			buffer.get(surplus, 0, remaining);
			buffer.clear();
			buffer.put(surplus);
		} else {
			buffer.clear();
		}

		return requests;
	}

	public ByteBuffer encode(Object src, SocketSession session) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(src);
			oos.close();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] bytes = bos.toByteArray();
		byte[] length = FinikesBytes.intToByte(bytes.length);
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length + length.length);
		buffer.put(length);
		buffer.put(bytes);
		buffer.flip();

		return buffer;
	}

	@Override
	public Object merge(Object stock, Object increment) throws CodecException {
		throw new CodecException("Object Stream can not merger");
	}
}