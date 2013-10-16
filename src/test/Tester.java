package test;

import org.finikes.tridge.aio.tcp.TcpProactor;
import org.finikes.tridge.tcp.codec.CodecFactory;
import org.finikes.tridge.tcp.handler.IoHandler;
import org.finikes.tridge.tcp.sub.codec.PrefixedBinaryCodecFactory;
import org.finikes.tridge.tcp.sub.codec.PrefixedStringCodec;
import org.finikes.tridge.tcp.sub.codec.PrefixedStringCodecFactory;
import org.finikes.tridge.tcp.sub.codec.TextlineCodec;
import org.finikes.tridge.tcp.sub.codec.TextlineCodecFactory;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int port = 8899;
		IoHandler handler = new TestHandler();
		CodecFactory codecFactory = PrefixedStringCodecFactory.getFactory();
		int dataLength = 256;

		// 采用默认线程池, port: 监听端口号; handler: 处理接口; codecFactory: 编解码协议; dataLength可不管
		try {
			new Thread(new TcpProactor(port, handler, codecFactory, dataLength))
					.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
