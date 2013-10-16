package org.finikes.tridge.aio.tcp;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import org.finikes.tridge.aio.tcp.session.SessionManager;
import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;

@SuppressWarnings("deprecation")
public class AsyncAccepter implements Runnable {
	private static final TcpReader READER = TcpReader.getInstance();
	private AsynchronousSocketChannel channel;

	protected AsyncAccepter(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}

	@Override
	public void run() {
		Thread currentThread = Thread.currentThread();
		int priority = Thread.MAX_PRIORITY;
		if (currentThread.getPriority() < priority) { // 设置accept线程为最大优先级
			currentThread.setPriority(priority);
		}

		ByteBuffer buffer = ByteBuffer.allocate(1024);
		SocketSession session = null;

		try {
			channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			channel.setOption(StandardSocketOptions.SO_SNDBUF, 16 * 1024);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			channel.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			session = new SocketSession(channel, buffer,
					TcpProactor.CODEC_FACTORY);
			if (TcpProactor.IS_OPEN_IDLE_DAEMON) {
				SessionManager.getInstance().in(session);
			}
		} catch (IOException e) {
			IoHandlerFactory.getInstance().exceptionCaught(session, e);
			return;
		}

		IoHandlerFactory.getInstance().channelOpen(session);

		channel.read(buffer, session, READER);

		TcpProactor.getInstance().pendingAccept();
	}

}
