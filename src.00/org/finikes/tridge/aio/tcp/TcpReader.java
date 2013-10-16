package org.finikes.tridge.aio.tcp;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;
import org.finikes.tridge.tcp.processor.Processor;

import org.finikes.util.thread.ProcessWorkerThreadPool;

public class TcpReader implements CompletionHandler<Integer, SocketSession> {
	private final SocketSession session;
	private final AsynchronousSocketChannel channel;
	private final Codec codec;

	public TcpReader(SocketSession session) {
		this.session = session;
		this.channel = session.getChannel();
		this.codec = session.getCodec();
	}

	private void close() {
		session.close(false);
	}

	public SocketSession getSession() {
		return session;
	}

	public void completed(Integer i, SocketSession session) {
		long now = System.currentTimeMillis();
		session.setLastOptTime(now);
		session.setLastReadTime(now);

		ByteBuffer buf = session.getBuffer();
		if (i > 0) {
			List<Object> requests = null;
			try {
				requests = codec.decode(session);
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

			channel.read(buf, session, this);
		} else if (i == -1) {
			try {
				close();
				buf = null;
			} catch (Exception e) {
				IoHandlerFactory.getInstance().exceptionCaught(session, e);
			}
		}
	}

	public void failed(Throwable exc, SocketSession session) {
		session.close(true);
		IoHandlerFactory.getInstance().exceptionCaught(session, exc);
	}
}