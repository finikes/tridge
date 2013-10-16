package org.finikes.tridge.tcp.sub.codec;

import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecFactory;

public class PrefixedBinaryCodecFactory extends CodecFactory {

	@Override
	public Codec getCodec() {
		return codec;
	}

	private PrefixedBinaryCodecFactory() {

	}

	private static final PrefixedBinaryCodecFactory factory = new PrefixedBinaryCodecFactory();
	private static final Codec codec = new PrefixedBinaryCodec();

	public static PrefixedBinaryCodecFactory getFactory() {
		return factory;
	}
}