package org.finikes.tridge.tcp.processor;

import java.util.List;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandler;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;

public class Processor implements Runnable {
	private final IoHandler handler = IoHandlerFactory.getInstance();

	private SocketSession session;
	private List<Object> requests;

	public Processor(SocketSession session) {
		this.session = session;
	}

	public Processor(SocketSession session, List<Object> requests) {
		this.session = session;
		this.requests = requests;
	}

	public void run() {
		process(requests);
	}

	public void process(List<Object> requests) {
		if (requests != null) {
			for (Object request : requests) {
				try {
					handler.messageReceived(session, request);
				} catch (Exception e) {
					IoHandlerFactory.getInstance().exceptionCaught(session, e);
				}
			}
		}
	}

	public void processor() {
		process(requests);
	}
}