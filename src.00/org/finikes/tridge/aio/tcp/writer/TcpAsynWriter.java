package org.finikes.tridge.aio.tcp.writer;

import java.util.concurrent.LinkedBlockingQueue;

import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;

public class TcpAsynWriter implements Runnable {
	private final LinkedBlockingQueue<WriteObject> messageQueue = new LinkedBlockingQueue<WriteObject>();

	public void run() {
		for (;;) {
			System.err.println("----");
			WriteObject writeObject = null;
			try {
				writeObject = messageQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}

			if (writeObject == null) {
				continue;
			}

			SocketSession session = writeObject.getSession();

			if (session.isClose()) {
				messageQueue.remove(writeObject);
				continue;
			}

			try {
				System.out.print(session.toString() + " ; ");
				p();
				TcpWriter.getWriter().write(writeObject, session);
			} catch (Exception e) {
				IoHandlerFactory.getInstance().exceptionCaught(session, e);
			}
		}
	}
	
	private static int i = 0;
	private static final synchronized void p(){
		i++;
		System.out.println(i);
	}

	public LinkedBlockingQueue<WriteObject> getQueue() {
		return messageQueue;
	}
}