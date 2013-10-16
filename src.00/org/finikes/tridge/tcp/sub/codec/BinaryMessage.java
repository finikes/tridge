package org.finikes.tridge.tcp.sub.codec;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.finikes.util.BinaryMessageUtils;

/**
 * type-def:
 * integer:	1
 * string:	2
 * short:	3
 * byte:	4
 * bool:	5
 * long:	6
 * @author Administrator
 *
 */
public class BinaryMessage {
	private static final Charset CHARSET = Charset.forName("UTF-8");

	private short actionId;

	private ByteBuffer data;

	private int paramsLength;

	private static final short NONE_TYPE = -1;

	private final List<byte[]> params = new ArrayList<byte[]>(16);

	public static final BinaryMessage NONE = new BinaryMessage(NONE_TYPE);

	public BinaryMessage() {
	}

	public ByteBuffer flush() {
		paramsLength = getBufferSize();
		this.data = ByteBuffer.wrap(new byte[paramsLength + 6]);
		this.data.order(ByteOrder.BIG_ENDIAN);
		this.data.putInt(paramsLength + 2);
		this.data.putShort(actionId);
		for (byte[] bs : params) {
			this.data.put(bs);
		}

		return data;
	}

	private int getBufferSize() {
		int size = 0;
		for (byte[] bs : params) {
			size = size + bs.length;
		}

		return size;
	}

	public BinaryMessage(short actionId) {
		this();
		this.actionId = actionId;
	}

	public boolean error() {
		return actionId == NONE_TYPE;
	}

	public void putInt(int intParam) {
		byte[] bs = BinaryMessageUtils.intToBytes((byte) 1, intParam,
				BinaryMessageUtils.BIG_ENDIAN);
		params.add(bs);
	}

	public void putLong(long longParam) {
		byte[] bs = BinaryMessageUtils.longToBytes((byte) 6, longParam,
				BinaryMessageUtils.BIG_ENDIAN);
		params.add(bs);
	}

	public int getInt() {
		data.get();
		return data.getInt();
	}

	public long getLong() {
		data.get();
		return data.getLong();
	}

	private static final byte TRUE = 1;
	private static final byte FALSE = 0;

	public void putBoolean(boolean b) {
		if (b)
			params.add(new byte[] { 5, TRUE });
		else
			params.add(new byte[] { 5, FALSE });
	}

	public boolean getBoolean() {
		data.get();
		return data.get() == TRUE;
	}

	public void put(byte b) {
		byte[] bs = new byte[] { 4, b };
		params.add(bs);
	}

	public byte get() {
		data.get();
		return data.get();
	}

	public void putShort(short shortParam) {
		byte[] bs = BinaryMessageUtils.shortToBytes((byte) 3, shortParam,
				BinaryMessageUtils.BIG_ENDIAN);
		params.add(bs);
	}

	public short getShort() {
		data.get();
		return data.getShort();
	}

	public void putString(String stringParam) {
		byte[] strBytes = stringParam.getBytes(CHARSET);

		int strLength = strBytes.length;
		byte[] bs = new byte[5 + strLength];
		bs[0] = 2;
		BinaryMessageUtils.fillInt(bs, 1, strLength,
				BinaryMessageUtils.BIG_ENDIAN);
		System.arraycopy(strBytes, 0, bs, 5, strLength);
		params.add(bs);
	}

	public String getString() {
		data.get();
		int strLen = data.getInt();
		byte[] strBytes = new byte[strLen];
		data.get(strBytes);
		String msgStr = new String(strBytes, CHARSET).trim();
		return msgStr;
	}

	public short getActionId() {
		return actionId;
	}

	public void setActionId(short actionId) {
		this.actionId = actionId;
	}

	public ByteBuffer getData() {
		ByteBuffer cloneData = data.duplicate();
		cloneData.rewind();
		return cloneData;
	}

	public void setData(ByteBuffer params) {
		this.data = params;
	}

	public int getDataLength() {
		return paramsLength;
	}
}
