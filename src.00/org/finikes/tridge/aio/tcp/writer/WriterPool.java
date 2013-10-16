package org.finikes.tridge.aio.tcp.writer;

import java.io.IOException;
import java.util.Random;

public class WriterPool {
	/*private static TcpAsynWriter[] writers;

	private static int size;

	public static void init(int size) throws IOException {
		writers = new TcpAsynWriter[size];
		WriterPool.size = size;
		for (int i = 0; i < size; i++) {
			TcpAsynWriter writer = new TcpAsynWriter();
			writers[i] = writer;
			Thread thread = new Thread(writer, "asynWriter-thread-" + i);
			thread.start();
		}
	}

	public static TcpAsynWriter getWriter() {
		return writers[random()];
	}

	private static Random random = new Random();*/
	
	private static TcpAsynWriter writer;

	private static int size;

	public static void init(int size) throws IOException {
		writer = new TcpAsynWriter();
	}

	public static TcpAsynWriter getWriter() {
		return writer;
	}
}
