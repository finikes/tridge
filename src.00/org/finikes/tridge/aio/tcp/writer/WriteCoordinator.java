package org.finikes.tridge.aio.tcp.writer;

import org.finikes.tridge.aio.tcp.session.SocketSession;

public class WriteCoordinator {
	protected static final void coordinate(boolean way, SocketSession session,
			WriteObject response) {
		if (way) {
			session.getWrittenStock().offer(response);// 两条语句的次序很重要
			session.setWriteLock(true);
			return;
		}

		session.setWriteLock(false);
	}
}
