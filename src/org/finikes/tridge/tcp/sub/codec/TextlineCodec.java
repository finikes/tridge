package org.finikes.tridge.tcp.sub.codec;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.List;

import org.finikes.tridge.aio.tcp.buffer.Buffers;
import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecException;
import org.finikes.utils.array.FinikesBytes;

public class TextlineCodec implements Codec {
	public static Charset charset;
	private static final byte[] initor = new byte[0];

	private byte[] tempQueue = initor;
	private static String delimiter = System.getProperty("line.separator");
	private final CharsetDecoder decoder = charset.newDecoder();

	public List<Object> decode(SocketSession session) {
		ByteBuffer buffer = session.getBuffer();

		byte[] array = Buffers.getArray(buffer);
		buffer.flip();

		tempQueue = FinikesBytes.sysJoint(tempQueue, array);
		String tempString = null;

		try {
			tempString = decoder.decode(ByteBuffer.wrap(tempQueue)).toString();
		} catch (MalformedInputException e) {
			// e.printStackTrace();
			return null;
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		}

		if (!tempString.contains(delimiter)) {
			return null;
		}

		String[] tempArray = tempString.split(delimiter);

		if (tempArray.length <= 0) {
			return null;
		}

		if (tempArray[0] == null) {
			return null;
		} else {
			boolean f = tempString.endsWith(delimiter);
			if (f) {
				tempQueue = initor;
				List<Object> results = new ArrayList<Object>(tempArray.length);
				for (String s : tempArray) {
					results.add(s);
				}

				return results;
			}

			else {
				String[] _results = new String[tempArray.length - 1];
				for (int i = 0; i < tempArray.length - 1; i++) {
					_results[i] = tempArray[i];
				}

				tempQueue = tempArray[tempArray.length - 1].getBytes();
				List<Object> results = new ArrayList<Object>(_results.length);
				for (String s : _results) {
					results.add(s);
				}

				return results;
			}
		}

	}

	@Override
	public ByteBuffer encode(Object src, SocketSession session) {
		byte[] bytes = (((String) src) + delimiter).getBytes(charset);
		return ByteBuffer.wrap(bytes);
	}

	@Override
	public Object merge(Object stock, Object increment) throws CodecException {
		String _stock = (String) stock;
		String _increment = (String) increment;
		return new StringBuilder(_stock).append(_increment).toString();
	}
}