package test;

import org.finikes.tridge.tcp.sub.codec.BinaryMessage;


public class TTT {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BinaryMessage bm = new BinaryMessage();
		bm.setActionId((short)123);
		bm.putInt(11);
		bm.putInt(22);
		bm.putString("abcf");
		
		bm.flush();
		bm.getData().flip();
		
		bm.getActionId();
		bm.getDataLength();
		System.out.println(bm.getInt());
		System.out.println(bm.getInt());
	}
}
