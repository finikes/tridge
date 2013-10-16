package org.finikes.tridge.aio.tcp.writer;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.TimeUnit;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;

public class TcpWriter0 {
	public static final int WRITE_SPIN_COUNT = Runtime.getRuntime()
			.availableProcessors() + 1;

	public void write(Object response, SocketSession session) {
		long now = System.currentTimeMillis();
		session.setLastOptTime(now);
		session.setLastWriteTime(now);

		ByteBuffer buf = session.getCodec().encode(response, session);
		AsynchronousSocketChannel socketChannel = session.getChannel();

		int localWrittenBytes = 0;

		/*
		 * for (int i = WRITE_SPIN_COUNT; i > 0; i--) { localWrittenBytes =
		 * socketChannel.write(buf); if (localWrittenBytes != 0 ||
		 * !buf.hasRemaining()) { return; } }
		 */

		try {
			if (session.isClose()) {
				IoHandlerFactory.getInstance().channelClose(session);
				return;
			}

			if (!socketChannel.isOpen()) {
				return;
			}

			localWrittenBytes = socketChannel.write(buf).get(5,
					TimeUnit.SECONDS);

			if (localWrittenBytes == 0) {
				session.asynWrite(response);
				return;
			}

			session.reduceResponseSize();
		} catch (Exception e) {
			IoHandlerFactory.getInstance().exceptionCaught(session, e);
		}
	}

	private TcpWriter0() {

	}

	private final static TcpWriter0 writer = new TcpWriter0();

	public static TcpWriter0 getWriter() {
		return writer;
	}
}
