package org.finikes.tridge.tcp.sub.codec;

import java.nio.charset.Charset;

import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecFactory;

public class TextlineCodecFactory extends CodecFactory {
	private static TextlineCodecFactory factory = new TextlineCodecFactory();

	private TextlineCodecFactory() {

	}

	@Override
	public Codec getCodec() {
		return new TextlineCodec();
	}

	public static TextlineCodecFactory getFactory(String encoding) {
		TextlineCodec.charset = Charset.forName(encoding);
		return factory;
	}
}
