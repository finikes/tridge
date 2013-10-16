package org.finikes.tridge.aio.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.finikes.tridge.aio.tcp.writer.WriterPool;
import org.finikes.tridge.tcp.codec.CodecFactory;
import org.finikes.tridge.tcp.handler.IoHandler;
import org.finikes.tridge.tcp.handler.IoHandlerFactory;
import org.finikes.util.thread.ProcessWorkerThreadPool;

public class TcpProactor implements Runnable {
	protected AsynchronousChannelGroup asynchronousChannelGroup;
	protected AsynchronousServerSocketChannel asynchronousServerSocketChannel;
	protected boolean started;

	private static int DEFAULT_THREAD_NUMBER = Runtime.getRuntime()
			.availableProcessors() + 1;

	protected static boolean SUPPORT_MULTI_PROCESS_THREAD_MODEL = false;
	// 取消底层的心跳检测 public static boolean IS_OPEN_IDLE_DAEMON;
	public static int IDLE_JUDGE_INTERVAL;
	public static int DATA_LENGTH;

	public static int PORT;

	public static CodecFactory CODEC_FACTORY;

	private static TcpProactor TCP_PROACTOR;

	public static TcpProactor getInstance() {
		return TCP_PROACTOR;
	}

	private ExecutorService exec;

	private void init(int port, IoHandler handler, CodecFactory codecFactory,
			int dataLength, ExecutorService exec, ExecutorService process,
			int processorCount, int writerThreadCount,
			boolean supportMultiProcessThreadModel, int idleJudgeInterval)
			throws IOException {
		PORT = port;
		DATA_LENGTH = dataLength;
		IoHandlerFactory.init(handler);
		this.exec = exec;
		ProcessWorkerThreadPool.setExec(process);
		WriterPool.init(writerThreadCount);
		SUPPORT_MULTI_PROCESS_THREAD_MODEL = supportMultiProcessThreadModel;
		IDLE_JUDGE_INTERVAL = idleJudgeInterval;

		/**
		 * 取消底层的心跳检测 if (idleJudgeInterval > 0) { IS_OPEN_IDLE_DAEMON = true;
		 * SessionManager.getInstance().launch(); }
		 */

		CODEC_FACTORY = codecFactory;

		TCP_PROACTOR = this;
	}

	public TcpProactor(int port, IoHandler handler, CodecFactory codecFactory,
			int dataLength) throws IOException, InstantiationException,
			IllegalAccessException {
		this(port, handler, codecFactory, dataLength, Executors
				.newFixedThreadPool(DEFAULT_THREAD_NUMBER), Executors
				.newFixedThreadPool(DEFAULT_THREAD_NUMBER),
				DEFAULT_THREAD_NUMBER, DEFAULT_THREAD_NUMBER, false, 0);
	}

	public TcpProactor(int port, IoHandler handler, CodecFactory codecFactory,
			int dataLength, int idleJudgeInterval) throws IOException,
			InstantiationException, IllegalAccessException {
		this(port, handler, codecFactory, dataLength, Executors
				.newFixedThreadPool(DEFAULT_THREAD_NUMBER), Executors
				.newFixedThreadPool(DEFAULT_THREAD_NUMBER),
				DEFAULT_THREAD_NUMBER, DEFAULT_THREAD_NUMBER, false,
				idleJudgeInterval);
	}

	public TcpProactor(int port, IoHandler handler, CodecFactory codecFactory,
			int dataLength, ExecutorService exec, ExecutorService process,
			int processorCount, int writerThreadCount,
			boolean supportMultiProcessThreadModel, int idleJudgeInterval)
			throws IOException, InstantiationException, IllegalAccessException {

		this.init(port, handler, codecFactory, dataLength, exec, process,
				processorCount, writerThreadCount,
				supportMultiProcessThreadModel, idleJudgeInterval);
	}

	public void run() {
		try {
			asynchronousChannelGroup = AsynchronousChannelGroup
					.withThreadPool(exec);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			asynchronousServerSocketChannel = AsynchronousServerSocketChannel
					.open(asynchronousChannelGroup);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			asynchronousServerSocketChannel.setOption(
					StandardSocketOptions.SO_REUSEADDR, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			asynchronousServerSocketChannel.setOption(
					StandardSocketOptions.SO_RCVBUF, 16 * 1024);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			asynchronousServerSocketChannel.bind(new InetSocketAddress(
					"0.0.0.0", PORT), DATA_LENGTH);
		} catch (IOException e) {
			e.printStackTrace();
		}

		started = true;

		pendingAccept();
	}

	private static final TcpAcceptor ACCEPTOR = TcpAcceptor.getInstance();

	public void pendingAccept() {
		if (this.started && this.asynchronousServerSocketChannel.isOpen()) {

			this.asynchronousServerSocketChannel.accept(null, ACCEPTOR);

			try {
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalStateException("Controller has been closed");
		}
	}
}