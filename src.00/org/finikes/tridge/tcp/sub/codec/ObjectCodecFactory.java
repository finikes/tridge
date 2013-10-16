package org.finikes.tridge.tcp.sub.codec;

import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecFactory;
import org.finikes.tridge.tcp.sub.codec.ObjectCodec;

public class ObjectCodecFactory extends CodecFactory {
	@Override
	public Codec getCodec() {
		return codec;
	}

	private ObjectCodecFactory() {

	}

	private static final ObjectCodecFactory factory = new ObjectCodecFactory();
	private static final Codec codec = new ObjectCodec();

	public static ObjectCodecFactory getFactory() {
		return factory;
	}
}
