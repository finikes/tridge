package org.finikes.util.thread;

import java.util.concurrent.ExecutorService;

public class ProcessWorkerThreadPool {
	private static ExecutorService exec;

	public static void work(Runnable runnable) {
		exec.execute(runnable);
	}

	public static void setExec(ExecutorService exec) {
		ProcessWorkerThreadPool.exec = exec;
	}
}
