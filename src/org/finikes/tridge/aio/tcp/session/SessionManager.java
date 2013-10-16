package org.finikes.tridge.aio.tcp.session;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.finikes.tridge.aio.tcp.TcpProactor;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;

@Deprecated
public class SessionManager implements Runnable {
	private final Map<String, SocketSession> sessionList = new ConcurrentHashMap<String, SocketSession>(
			102400);

	private final long idleJudgeInterval = TcpProactor.IDLE_JUDGE_INTERVAL * 60 * 1000;

	public SocketSession getSession(String sessionId) {
		return sessionList.get(sessionId);
	}

	public void removeSession(SocketSession session) {
		sessionList.remove(session.toString());
	}

	private void check() {
		Collection<SocketSession> sessions = sessionList.values();

		long now = System.currentTimeMillis();

		for (SocketSession session : sessions) {
			long sessionReadIdleTime = session.getLastReadTime()
					+ idleJudgeInterval;

			long sessionWriteIdleTime = session.getLastWriteTime()
					+ idleJudgeInterval;

			boolean readIsIdle = false;
			boolean writeIsIdle = false;
			boolean optIsIdle = false;

			if (now >= sessionReadIdleTime) {
				readIsIdle = true;
			}

			if (now >= sessionWriteIdleTime) {
				writeIsIdle = true;
			}

			if (readIsIdle && writeIsIdle) {
				optIsIdle = true;
			}

			if (optIsIdle) {
				if (!session.isClose()) {
					IoHandlerFactory.getInstance().channelIdle(session,
							SessionIdleType.OPT_IDLE_TYPE);
				}

				return;
			}

			if (readIsIdle) {
				if (!session.isClose()) {
					IoHandlerFactory.getInstance().channelIdle(session,
							SessionIdleType.READ_IDLE_TYPE);
				}

				return;
			}

			if (writeIsIdle) {
				if (!session.isClose()) {
					IoHandlerFactory.getInstance().channelIdle(session,
							SessionIdleType.WRITE_IDLE_TYPE);
				}

				return;
			}
		}
	}

	public void in(SocketSession session) {
		sessionList.put(session.toString(), session);
	}

	public void run() {
		for (;;) {
			check();

			try {
				Thread.sleep(60 * 1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private SessionManager() {

	}

	public void launch() {
		new Thread(this).start();
	}

	private static SessionManager sessionContainer = new SessionManager();

	public static SessionManager getInstance() {
		return sessionContainer;
	}
}
