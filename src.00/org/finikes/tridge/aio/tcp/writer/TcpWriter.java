package org.finikes.tridge.aio.tcp.writer;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.finikes.tridge.log.Logger;
import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;

public class TcpWriter {
	public static final int WRITE_SPIN_COUNT = Runtime.getRuntime()
			.availableProcessors() + 1;

	public int write(WriteObject response, SocketSession session) {
		long now = System.currentTimeMillis();
		session.setLastOptTime(now);
		session.setLastWriteTime(now);

		// ByteBuffer buf = session.getCodec().encode(response, session);
		AsynchronousSocketChannel socketChannel = session.getChannel();

		int localWrittenBytes = 0;

		/*
		 * for (int i = WRITE_SPIN_COUNT; i > 0; i--) { localWrittenBytes =
		 * socketChannel.write(buf); if (localWrittenBytes != 0 ||
		 * !buf.hasRemaining()) { return; } }
		 */

		if (session.isClose()) {
			IoHandlerFactory.getInstance().channelClose(session);
			session.getWriter().getQueue().remove(session);
			return WriteStatus.SESSION_FAIL;
		}

		if (!socketChannel.isOpen()) {
			session.getWriter().getQueue().remove(session);
			return WriteStatus.SESSION_FAIL;
		}

		ByteBuffer buffer = response.getResponse();
		try {
			int remaining = buffer.remaining();
			localWrittenBytes = socketChannel.write(buffer).get(5,
					TimeUnit.SECONDS);
			if (localWrittenBytes == remaining || buffer.remaining() == 0) {
				IoHandlerFactory.getInstance().messageSent(session,
						response.getSrc());
			}
		} catch (Exception e) {
			IoHandlerFactory.getInstance().exceptionCaught(session, e);
			return WriteStatus.SESSION_FAIL;
		}

		if (localWrittenBytes == 0) {
			System.err.println(" - - - ");
			Logger.log("Channel Blocking -Response: " + response + " -Time: "
					+ new Date());
			WriteCoordinator.coordinate(true, session, response);
			return WriteStatus.BLOCKING;
		}

		session.reduceResponseSize();

		return WriteStatus.OK;
	}

	public int write(SocketSession session) {
		LinkedBlockingQueue<WriteObject> backlogs = session.getWrittenStock();
		for (int i = 0; i < 100000; i++) {
			WriteObject response = backlogs.peek();
			if (response == null) {
				return WriteStatus.OK;
			}

			int writeStatus = this.write(response, session);

			if (writeStatus == WriteStatus.BLOCKING) {
				return WriteStatus.BLOCKING;
			}

			if (writeStatus == WriteStatus.SESSION_FAIL) {
				return WriteStatus.SESSION_FAIL;
			}

			backlogs.remove();
		}

		return WriteStatus.BLOCKING;
	}

	private TcpWriter() {

	}

	private final static TcpWriter writer = new TcpWriter();

	public static TcpWriter getWriter() {
		return writer;
	}
}
