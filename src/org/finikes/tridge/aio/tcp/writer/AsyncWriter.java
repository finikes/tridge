package org.finikes.tridge.aio.tcp.writer;

import java.nio.channels.CompletionHandler;
import java.util.Queue;

import org.finikes.tridge.aio.tcp.TcpProactor;
import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;

public class AsyncWriter implements CompletionHandler<Integer, SocketSession> {
	private static final AsyncWriter INSTANCE = new AsyncWriter();

	public static final AsyncWriter getInstance() {
		return INSTANCE;
	}

	/**
	 * 需要判断结果result是否小于0, 如果小于0就表示对端关闭了
	 */
	@Override
	public void completed(Integer result, SocketSession session) {
		if (result < 0) {
			session.close(false);
			return;
		}

		if (TcpProactor.IS_OPEN_IDLE_DAEMON) {
			long now = System.currentTimeMillis();
			session.setLastOptTime(now);
			session.setLastWriteTime(now);
		}

		WriteObject wo = null;
		Queue<WriteObject> writeQueue = session.getWrittenQueue();
//synchronized (writeQueue) {
	

		wo = writeQueue.peek();
		if (wo.getResponse() == null || !wo.getResponse().hasRemaining()) {
			writeQueue.remove();

			IoHandlerFactory.getInstance().messageSent(session, wo.getSrc());
			wo = writeQueue.peek();
		}
//}
		if (wo != null) {
			//session.getPendingWrite().compareAndSet(false, true);
			session.pendingWrite(wo, true);
		} else {
			session.getPendingWrite().compareAndSet(true, false);
		}
	}

	@Override
	public void failed(Throwable exc, SocketSession attachment) {
		IoHandlerFactory.getInstance().exceptionCaught(attachment, exc);
	}
}
