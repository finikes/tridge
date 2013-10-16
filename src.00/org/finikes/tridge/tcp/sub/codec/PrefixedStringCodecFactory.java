package org.finikes.tridge.tcp.sub.codec;

import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecFactory;

public class PrefixedStringCodecFactory extends CodecFactory {

	@Override
	public Codec getCodec() {
		return codec;
	}

	private PrefixedStringCodecFactory() {

	}

	private static final PrefixedStringCodecFactory factory = new PrefixedStringCodecFactory();
	private static final Codec codec = new PrefixedStringCodec();

	public static PrefixedStringCodecFactory getFactory() {
		return factory;
	}
}