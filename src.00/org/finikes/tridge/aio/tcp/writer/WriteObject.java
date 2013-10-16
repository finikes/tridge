package org.finikes.tridge.aio.tcp.writer;

import java.nio.ByteBuffer;

import org.finikes.tridge.aio.tcp.session.SocketSession;

public class WriteObject {
	public WriteObject(SocketSession session, ByteBuffer response, Object src) {
		this.session = session;
		this.response = response;
		this.src = src;
	}

	private final SocketSession session;

	private final ByteBuffer response;

	private final Object src;

	public Object getSrc() {
		return src;
	}

	public SocketSession getSession() {
		return session;
	}

	public ByteBuffer getResponse() {
		return response;
	}
}
