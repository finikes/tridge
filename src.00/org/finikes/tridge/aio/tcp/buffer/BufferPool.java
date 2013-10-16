package org.finikes.tridge.aio.tcp.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class BufferPool {
	private static int poolSize = 16;
	private static LinkedBlockingQueue<ByteBuffer> size64 = new LinkedBlockingQueue<ByteBuffer>();
	private static LinkedBlockingQueue<ByteBuffer> size128 = new LinkedBlockingQueue<ByteBuffer>();
	private static LinkedBlockingQueue<ByteBuffer> size256 = new LinkedBlockingQueue<ByteBuffer>();
	private static LinkedBlockingQueue<ByteBuffer> size512 = new LinkedBlockingQueue<ByteBuffer>();
	private static LinkedBlockingQueue<ByteBuffer> size1024 = new LinkedBlockingQueue<ByteBuffer>();
	private static LinkedBlockingQueue<ByteBuffer> size2048 = new LinkedBlockingQueue<ByteBuffer>();
	private static LinkedBlockingQueue<ByteBuffer> size4096 = new LinkedBlockingQueue<ByteBuffer>();

	public static void init() {
		for (int i = 0; i < poolSize; i++) {
			size64.offer(ByteBuffer.allocate(64));
			size128.offer(ByteBuffer.allocate(128));
			size256.offer(ByteBuffer.allocate(256));
			size512.offer(ByteBuffer.allocate(512));
			size1024.offer(ByteBuffer.allocate(1024));
			size2048.offer(ByteBuffer.allocate(2048));
			size4096.offer(ByteBuffer.allocate(4096));
		}
	}

	public static ByteBuffer getBuffer(int capacity) {
		LinkedBlockingQueue<ByteBuffer> pool = getParentPool(capacity);
		if (pool != null) {
			try {
				return pool.take();
			} catch (InterruptedException e) {
				return ByteBuffer.allocate(capacity);
			}
		}

		return ByteBuffer.allocate(capacity);
	}

	public static boolean revert(ByteBuffer buffer) {
		LinkedBlockingQueue<ByteBuffer> pool = getParentPool(buffer.capacity());
		if (pool != null) {
			buffer.clear();
			return pool.offer(buffer);
		}

		return true;
	}

	private static LinkedBlockingQueue<ByteBuffer> getParentPool(int size) {
		if (size <= 64) {
			return size64;
		}
		if (size <= 128) {
			return size128;
		}
		if (size <= 256) {
			return size256;
		}
		if (size <= 512) {
			return size512;
		}
		if (size <= 1024) {
			return size1024;
		}
		if (size <= 2048) {
			return size2048;
		}
		if (size <= 4096) {
			return size4096;
		}

		return null;
	}
}
