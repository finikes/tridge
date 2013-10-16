package org.finikes.tridge.aio.tcp;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.finikes.util.thread.AcceptThreadPool;

public class TcpAcceptor implements
		CompletionHandler<AsynchronousSocketChannel, Object> {

	private static final TcpAcceptor TCP_ACCEPTOR = new TcpAcceptor();

	protected static TcpAcceptor getInstance() {
		return TCP_ACCEPTOR;
	}

	@Override
	public void completed(AsynchronousSocketChannel channel, Object attachment) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		AcceptThreadPool.work(new AsyncAccepter(channel));
	}

	@Override
	public void failed(Throwable exc, Object attachment) {
		exc.printStackTrace();
		TcpProactor.getInstance().pendingAccept();
	}

}
