package org.finikes.tridge.aio.tcp;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.List;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;
import org.finikes.tridge.tcp.processor.Processor;

import org.finikes.util.thread.ProcessWorkerThreadPool;

public class TcpReader implements CompletionHandler<Integer, SocketSession> {
	private static final TcpReader INSTANCE = new TcpReader();

	public static final TcpReader getInstance() {
		return INSTANCE;
	}

	public void completed(Integer i, SocketSession session) {
		if (TcpProactor.IS_OPEN_IDLE_DAEMON) {
			long now = System.currentTimeMillis();
			session.setLastOptTime(now);
			session.setLastReadTime(now);
		}

		ByteBuffer buf = session.getBuffer();
		if (i > 0) {
			List<Object> requests = null;
			try {
				requests = session.getCodec().decode(session);
			} catch (Exception e) {
				session.close(false);
				IoHandlerFactory.getInstance().exceptionCaught(session, e);
				return;
			}

			if (requests != null) {
				if (requests.size() > 0) {
					if (!TcpProactor.SUPPORT_MULTI_PROCESS_THREAD_MODEL) {
						Processor processor = session.getProcessor();
						processor.process(requests);
					} else {
						Processor processor = new Processor(session, requests);
						ProcessWorkerThreadPool.work(processor);
					}
				}
			}

			session.getChannel().read(buf, session, this);
		} else if (i < 0) {// or == -1?
			try {
				session.close(false);
				buf = null;
			} catch (Exception e) {
				IoHandlerFactory.getInstance().exceptionCaught(session, e);
			}
		}
	}

	public void failed(Throwable exc, SocketSession session) {
		session.close(false);
		IoHandlerFactory.getInstance().exceptionCaught(session, exc);
	}
}