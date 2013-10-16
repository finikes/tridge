package org.finikes.tridge.tcp.sub.codec;

public class ThreadAction {// 操作查看JVM虚拟机中所的线程和线程组的类
	// 显示线程信息
	private void threadMessage(Thread thread, String index) {
		if (thread == null)
			return;
		buildReport(index + "ThreadName: " + thread.getName() + "  Priority: "
				+ thread.getPriority() + (thread.isDaemon() ? " Daemon" : "")
				+ (thread.isAlive() ? "" : " Inactive"));
	}

	// 显示线程组信息
	private void threadGroupMessage(ThreadGroup group, String index) {
		if (group == null)
			return; // 判断线程组
		int count = group.activeCount(); // 获得活动的线程数
		// 获得活动的线程组数
		int countGroup = group.activeGroupCount();
		// 根据活动的线程数创建指定个数的线程数组
		Thread[] threads = new Thread[count];
		// 根据活动的线程组数创建指定个数的线程组数组
		ThreadGroup[] groups = new ThreadGroup[countGroup];
		group.enumerate(threads, false); // 把所有活动子组的引用复制到指定数组中，false表示不包括对子组的所有活动子组的引用
		group.enumerate(groups, false);
		buildReport(index + "ThreadGroupName: " + group.getName()
				+ "MaxPriority: " + group.getMaxPriority()
				+ (group.isDaemon() ? " Daemon" : ""));
		// 循环显示当前活动的线程信息
		for (int i = 0; i < count; i++)
			threadMessage(threads[i], index + "    ");
		for (int i = 0; i < countGroup; i++)
			// 循环显示当前活动的线程组信息
			threadGroupMessage(groups[i], index + "    ");// 递归调用方法
	}

	public void threadsList() { // 找到根线程组并列出它递归的信息
		ThreadGroup currentThreadGroup; // 当前线程组
		ThreadGroup rootThreadGroup; // 根线程组
		ThreadGroup parent;
		// 获得当前活动的线程组
		currentThreadGroup = Thread.currentThread().getThreadGroup();
		rootThreadGroup = currentThreadGroup; // 获得根线程组
		parent = rootThreadGroup.getParent(); // 获得根线程
		while (parent != null) { // 循环对根线程组重新赋值
			rootThreadGroup = parent;
			parent = parent.getParent();
		}
		threadGroupMessage(rootThreadGroup, ""); // 显示根线程组
	}

	private StringBuffer report = new StringBuffer();

	private static String delimiter = System.getProperty("line.separator");

	private void buildReport(String subReport) {
		report = report.append(subReport).append(delimiter);
	}

	public static String exe() { // java程序主入口处
		ThreadAction action = new ThreadAction(); // 调用方法显示所有线程的信息
		action.threadsList();
		return action.report.toString();
	}
}