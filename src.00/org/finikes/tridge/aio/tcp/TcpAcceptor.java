package org.finikes.tridge.aio.tcp;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;
import org.finikes.util.thread.ProcessWorkerThreadPool;

public class TcpAcceptor implements
		CompletionHandler<AsynchronousSocketChannel, Object> {

	private static final TcpAcceptor TCP_ACCEPTOR = new TcpAcceptor();

	protected static TcpAcceptor getInstance() {
		return TCP_ACCEPTOR;
	}

	@Override
	public void completed(AsynchronousSocketChannel channel, Object attachment) {
		ProcessWorkerThreadPool.work(new AcceptInfo(channel));
	}

	@Override
	public void failed(Throwable exc, Object attachment) {
		// exc.printStackTrace();
		TcpProactor.getInstance().pendingAccept();
	}

}
