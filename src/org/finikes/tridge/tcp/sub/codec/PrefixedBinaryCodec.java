package org.finikes.tridge.tcp.sub.codec;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecException;
import org.finikes.util.BinaryMessageUtils;
import org.finikes.utils.array.FinikesBytes;

public class PrefixedBinaryCodec implements Codec {

	@Override
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

					BinaryMessage message = new BinaryMessage();

					byte[] _actionId = new byte[2];
					_actionId[0] = msgTmp[0];
					_actionId[1] = msgTmp[1];
					short actionId = BinaryMessageUtils.bytesToShort(_actionId,
							BinaryMessageUtils.BIG_ENDIAN);
					message.setActionId(actionId);

					byte[] data = FinikesBytes.part(msgTmp, 2,
							msgTmp.length - 2);

					ByteBuffer params = ByteBuffer.wrap(data);
					params.order(ByteOrder.BIG_ENDIAN);
					message.setData(params);

					requests.add(message);

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
				byte[] msgTmp = new byte[msgLength];
				buffer.get(msgTmp, 0, msgLength);

				BinaryMessage message = new BinaryMessage();
				byte[] _actionId = FinikesBytes.part(msgTmp, 0, 2);
				short actionId = BinaryMessageUtils.bytesToShort(_actionId,
						BinaryMessageUtils.BIG_ENDIAN);
				message.setActionId(actionId);

				byte[] data = FinikesBytes.part(msgTmp, 2, msgTmp.length - 2);

				ByteBuffer params = ByteBuffer.wrap(data);
				params.order(ByteOrder.BIG_ENDIAN);
				message.setData(params);

				requests.add(message);
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

	@Override
	public ByteBuffer encode(Object src, SocketSession session) {
		BinaryMessage msg = (BinaryMessage) src;
		ByteBuffer buffer = msg.flush();

		if (buffer.position() != 0)
			buffer.flip();

		return buffer;
	}

	@Override
	@Deprecated
	public Object merge(Object stock, Object increment) throws CodecException {
		byte[] _stock = (byte[]) stock;
		byte[] _increment = (byte[]) increment;

		return FinikesBytes.sysJoint(_stock, _increment);
	}

}
