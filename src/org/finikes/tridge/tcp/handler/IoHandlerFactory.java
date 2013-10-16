package org.finikes.tridge.tcp.handler;

public class IoHandlerFactory {
	private static IoHandler handler;

	public static void init(IoHandler handler) {
		IoHandlerFactory.handler = handler;
	}

	public static IoHandler getInstance() {
		return handler;
	}
}