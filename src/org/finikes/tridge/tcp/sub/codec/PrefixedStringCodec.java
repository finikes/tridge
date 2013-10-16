package org.finikes.tridge.tcp.sub.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecException;
import org.finikes.utils.array.FinikesBytes;

public class PrefixedStringCodec implements Codec {

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
					try {
						buffer.get(msgPartBytes, 0, bufferCapacity);
					} catch (RuntimeException e) {
						throw e;
					}

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

					requests.add(new String(msgTmp));

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
				requests.add(new String(message));
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
		String s = (String) src;

		byte[] bytes = s.getBytes();
		int length = bytes.length;
		byte[] lengthIntBytes = FinikesBytes.intToByte(length);
		ByteBuffer buffer = ByteBuffer.allocate(length + 4);
		buffer.put(lengthIntBytes);
		buffer.put(bytes);
		if (buffer.position() != 0)
			buffer.flip();

		return buffer;
	}

	@Override
	public Object merge(Object stock, Object increment) throws CodecException {
		String _stock = (String) stock;
		String _increment = (String) increment;
		return new StringBuilder(_stock).append(_increment).toString();
	}
}