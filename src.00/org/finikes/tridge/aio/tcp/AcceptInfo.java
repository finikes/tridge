package org.finikes.tridge.aio.tcp;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;

public class AcceptInfo implements Runnable {
	private AsynchronousSocketChannel channel;
	
	protected AcceptInfo(AsynchronousSocketChannel channel){
		this.channel = channel;
	}

	@Override
	public void run() {
		ByteBuffer buffer = ByteBuffer.allocate(128);
		SocketSession session = null;

		try {
			channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			session = new SocketSession(channel, buffer,
					TcpProactor.CODEC_FACTORY);
		} catch (IOException e) {
			IoHandlerFactory.getInstance().exceptionCaught(session, e);
			return;
		}

		IoHandlerFactory.getInstance().channelOpen(session);

		channel.read(buffer, session, new TcpReader(session));

		TcpProactor.getInstance().pendingAccept();
	}

}
