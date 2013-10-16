package org.finikes.util.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AcceptThreadPool {
	private static final ExecutorService exec = Executors.newSingleThreadExecutor();

	public static void work(Runnable runnable) {
		exec.execute(runnable);
	}
}
