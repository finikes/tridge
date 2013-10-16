package org.finikes.tridge.tcp.sub.codec;

import org.finikes.tridge.tcp.codec.Codec;
import org.finikes.tridge.tcp.codec.CodecFactory;
import org.finikes.tridge.tcp.sub.codec.HttpProtocolCodec;

public class HttpProtocolCodecFactory extends CodecFactory {
	private static final Codec codec = new HttpProtocolCodec();
	
	private HttpProtocolCodecFactory() {
		
	}
	
	private static HttpProtocolCodecFactory factory = new HttpProtocolCodecFactory();
	
	public static HttpProtocolCodecFactory getFactory(){
		return factory;
	}

	@Override
	public Codec getCodec() {
		return codec;
	}

}
