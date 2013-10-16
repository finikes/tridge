package org.finikes.tridge.aio.tcp.writer;

public interface WriteStatus {
	public static final int OK = 1;
	public static final int BLOCKING = 2;
	public static final int SESSION_FAIL = 3;
}
