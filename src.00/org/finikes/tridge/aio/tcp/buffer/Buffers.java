package org.finikes.tridge.aio.tcp.buffer;

import java.nio.ByteBuffer;

public class Buffers {

	/**
	 * 
	 * @param buffer
	 *            原始buffer
	 * @return 去除多余空位后的byte[]
	 * 
	 */
	public static byte[] getArray0(ByteBuffer buffer) {
		buffer.flip();
		byte[] array = new byte[buffer.remaining()];
		buffer.get(array);
		return array;
	}

	public static byte[] getArray(ByteBuffer buffer) {
		int position = buffer.position();
		byte[] array = new byte[position];

		for (int i = 0; i < position; i++) {
			array[i] = buffer.get(i);
		}

		return array;
	}

	@Deprecated
	public static ByteBuffer put(ByteBuffer src, ByteBuffer dist) {
		while (dist.remaining() > src.remaining()) {
			// src 扩容
		}

		return src.put(dist);
	}
}