package test;

import java.nio.charset.Charset;
import java.util.Date;

import org.finikes.tridge.aio.tcp.session.SessionIdleType;
import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.handler.IoHandlerAdapter;
import org.finikes.tridge.tcp.sub.codec.BinaryMessage;

public class TestHandler extends IoHandlerAdapter {
	private static long n = 0;
	private static int l = 0;
	private static final synchronized void t(){
		l++;
		if (l == 1) {
			n = System.currentTimeMillis();
		}
		//System.out.println(i);
		if (l == 100) {
			//b = true;
			System.out.println(" Time : "+(System.currentTimeMillis() - n));
		}
		
		if (l == 200) {
			//b = true;
			System.out.println(" Time : "+(System.currentTimeMillis() - n));
		}
		
		if (l == 300) {
			//b = true;
			System.out.println(" Time : "+(System.currentTimeMillis() - n));
		}
	}
	
	@Override
	public void channelClose(SocketSession session) {
		// 通道关闭
		System.out.println(session + " close!");
	}

	@Override
	public void channelIdle(SocketSession session, SessionIdleType idleType) {
		// 通道钝化
	}

	@Override
	public void channelOpen(SocketSession session) {
		System.out.println(session + " OPEN!");
		// 通道建立
		/*try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (true){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		//t();
		session.write("abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc");
		
	}

	@Override
	public void exceptionCaught(SocketSession session, Throwable caught) {
		// 发生异常
		caught.printStackTrace();
		
		// 关闭会话
		session.close(true);
	}

	@Override
	public void messageReceived(SocketSession session, Object msg) {
		// 消息到达
		if (!b){
		//session.write(msg);
		p();
		}
		//System.out.println(msg + " ************** ");
	}
	
	@Override
	public void messageSent(SocketSession session, Object msg){
		// 消息发出
		//System.out.println(msg);
	}
	
	private static int i = 0;
	private static boolean b = false;
	private static long s = 0;
	private static final synchronized void p(){
		i++;
		if (i == 1) {
			s = System.currentTimeMillis();
		}
		//System.out.println(i);
		if (i == 2000000) {
			b = true;
			System.out.println(" Time : "+(System.currentTimeMillis() - s));
		}
	}
}
