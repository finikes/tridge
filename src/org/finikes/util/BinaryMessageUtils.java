package org.finikes.util;

public class BinaryMessageUtils {
	static private short makeShort(byte b1, byte b0) {
		return (short) ((b1 << 8) | (b0 & 0xff));
	}

	static private int makeInt(byte b3, byte b2, byte b1, byte b0) {
		return (((b3) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff)));
	}

	private static byte short1(short x) {
		return (byte) (x >> 8);
	}

	private static byte short0(short x) {
		return (byte) (x);
	}

	private static byte int3(int x) {
		return (byte) (x >> 24);
	}

	private static byte int2(int x) {
		return (byte) (x >> 16);
	}

	private static byte int1(int x) {
		return (byte) (x >> 8);
	}

	private static byte int0(int x) {
		return (byte) (x);
	}

	private static byte char1(char x) {
		return (byte) (x >> 8);
	}

	private static byte char0(char x) {
		return (byte) (x);
	}

	static private char makeChar(byte b1, byte b0) {
		return (char) ((b1 << 8) | (b0 & 0xff));
	}

	static int bytesToInt(byte[] bs, boolean order) {
		if (order == BIG_ENDIAN) {
			return makeInt(bs[3], bs[2], bs[1], bs[0]);
		}

		return makeInt(bs[0], bs[1], bs[2], bs[3]);
	}

	public static void fillInt(byte[] bs, int index, int x, boolean order) {
		if (order == BIG_ENDIAN) {
			bs[index + 3] = int0(x);
			bs[index + 2] = int1(x);
			bs[index + 1] = int2(x);
			bs[index] = int3(x);

			return;
		}

		bs[index + 3] = int3(x);
		bs[index + 2] = int2(x);
		bs[index + 1] = int1(x);
		bs[index] = int0(x);
	}

	public static short bytesToShort(byte[] bs, boolean order) {
		if (order == LITTLE_ENDIAN) {
			return makeShort(bs[1], bs[0]);
		}

		return makeShort(bs[0], bs[1]);
	}

	public static char bytesToChar(byte[] bs, boolean order) {
		if (order == LITTLE_ENDIAN) {
			return makeChar(bs[1], bs[0]);
		}

		return makeChar(bs[0], bs[1]);
	}

	public static byte[] shortToBytes(byte type, short s, boolean order) {
		byte[] bs = new byte[3];
		bs[0] = type;
		if (order == LITTLE_ENDIAN) {
			bs[1] = short0(s);
			bs[2] = short1(s);
			return bs;
		}

		bs[1] = short1(s);
		bs[2] = short0(s);
		return bs;
	}

	public static byte[] charToBytes(char c, boolean order) {
		byte[] bs = new byte[2];
		if (order == LITTLE_ENDIAN) {
			bs[0] = char0(c);
			bs[1] = char1(c);
			return bs;
		}

		bs[0] = char1(c);
		bs[1] = char0(c);
		return bs;
	}

	public static byte[] intToBytes(byte type, int s, boolean order) {
		byte[] bs = new byte[5];
		bs[0] = type;
		if (order == LITTLE_ENDIAN) {
			bs[1] = int0(s);
			bs[2] = int1(s);
			bs[3] = int2(s);
			bs[4] = int3(s);
			return bs;
		}

		bs[1] = int3(s);
		bs[2] = int2(s);
		bs[3] = int1(s);
		bs[4] = int0(s);
		return bs;
	}

	public static final boolean BIG_ENDIAN = true;
	public static final boolean LITTLE_ENDIAN = false;

	public static final long bytesToLong(byte[] bs, boolean order) {
		if (order) {
			return bytesToLong0(bs);
		}

		return bytesToLong1(bs);
	}

	private static final long bytesToLong0(byte[] bs) {
		long l = 0;

		l = bs[0];

		l |= ((long) bs[1] << 8);

		l |= ((long) bs[2] << 16);

		l |= ((long) bs[3] << 24);

		l |= ((long) bs[4] << 32);

		l |= ((long) bs[5] << 40);

		l |= ((long) bs[6] << 48);

		l |= ((long) bs[7] << 56);

		return l;
	}

	private static final long bytesToLong1(byte[] bs) {
		long l = 0;

		l = bs[7];

		l |= ((long) bs[6] << 8);

		l |= ((long) bs[5] << 16);

		l |= ((long) bs[4] << 24);

		l |= ((long) bs[3] << 32);

		l |= ((long) bs[2] << 40);

		l |= ((long) bs[1] << 48);

		l |= ((long) bs[0] << 56);

		return l;
	}

	public static byte[] longToBytes(byte type, long s, boolean order) {
		byte[] bs = new byte[9];

		bs[0] = type;

		if (order) {
			for (int i = 8; i > 0; i--) {
				bs[i] = new Long(s & 0xff).byteValue();

				s = s >> 8;
			}
		} else {
			for (int i = 1; i < 9; i++) {
				bs[i] = new Long(s & 0xff).byteValue();

				s = s >> 8;
			}
		}

		return bs;
	}
}
