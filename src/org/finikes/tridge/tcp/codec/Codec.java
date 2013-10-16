package org.finikes.tridge.tcp.codec;

import java.nio.ByteBuffer;
import java.util.List;

import org.finikes.tridge.aio.tcp.session.SocketSession;

public interface Codec {
	public List<Object> decode(SocketSession session);

	public ByteBuffer encode(Object src, SocketSession session);

	public Object merge(Object stock, Object increment) throws CodecException;
}