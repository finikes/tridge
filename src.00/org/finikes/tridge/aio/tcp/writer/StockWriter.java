package org.finikes.tridge.aio.tcp.writer;

import java.util.concurrent.LinkedBlockingQueue;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;

public class StockWriter {
	private final LinkedBlockingQueue<SocketSession> backlogsQueue = new LinkedBlockingQueue<SocketSession>();

	public void run() {
		for (;;) {
			SocketSession session = null;
			try {
				session = backlogsQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}

			if (session == null) {
				continue;
			}

			if (session.isClose()) {
				continue;
			}

			try {
				int writeStatus = TcpWriter.getWriter().write(session);
				if (writeStatus == WriteStatus.BLOCKING) {
					backlogsQueue.offer(session);
				}
			} catch (Exception e) {
				IoHandlerFactory.getInstance().exceptionCaught(session, e);
			}
		}
	}

	public LinkedBlockingQueue<SocketSession> getQueue() {
		return backlogsQueue;
	}
}
