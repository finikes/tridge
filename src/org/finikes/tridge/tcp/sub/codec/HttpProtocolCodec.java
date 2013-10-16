package org.finikes.tridge.tcp.sub.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.finikes.tridge.aio.tcp.buffer.Buffers;
import org.finikes.tridge.aio.tcp.session.SocketSession;
import org.finikes.tridge.tcp.codec.Codec;

public class HttpProtocolCodec implements Codec {
	private static final String SEPARATOR = "\r\n\r\n";

	public List<Object> decode(SocketSession session) {
		ByteBuffer buffer = session.getBuffer();

		byte[] array = Buffers.getArray(buffer);
		buffer.flip();

		String tmp = new String(array);
		String tmpString = (String) session.getAttachment("tmpString");
		if (tmpString != null) {
			tmp = tmpString + tmp;
		}

		String method = new StringTokenizer(tmp).nextElement().toString();

		String[] requestTmp = null;
		List<Object> results = new ArrayList<Object>();

		while ((requestTmp = getHttpRequest(tmp, method)) != null) {
			if (requestTmp.length > 1) {
				tmp = requestTmp[1];
				results.add(requestTmp[0]);
			} else {
				results.add(requestTmp[0]);
				break;
			}
		}

		session.setAttachment("tmpString", tmp);

		return results;
	}

	private String[] getHttpRequest(String request, String method) {
		if (request.contains(SEPARATOR)) {
			String[] tmp = request.split(SEPARATOR);

			if ("GET".equals(method)) {
				return tmp;
			}

			if ("POST".equals(method)) {
				// 获取Content-Length值
				String[] requestMsgs = tmp[0].split("\r\n");
				Map<String, String> msgs = new HashMap<String, String>();
				for (int i = 1; i < requestMsgs.length; i++) {
					String[] msg = requestMsgs[i].split(": ");

					msgs.put(msg[0].toLowerCase(), msg[1]);
				}
				int contentLength = Integer.parseInt(msgs.get("Content-Length"
						.toLowerCase()));

				if (tmp.length == 2) {
					String params = tmp[1];

					if (params != null) {
						String[] contextTmp = new String[2];

						if (params.length() < contentLength) {
							return null;
						}

						String content = params.substring(0, contentLength);
						contextTmp[0] = tmp[0] + SEPARATOR + content;
						contextTmp[1] = params.substring(contentLength, params
								.length());

						return contextTmp;
					}
				}
			}
		}

		return null;
	}

	public ByteBuffer encode(Object src, SocketSession session) {
		return ByteBuffer.wrap(((String) src).getBytes());
	}

	@Override
	public Object merge(Object stock, Object increment) {
		String _stock = (String) stock;
		String _increment = (String) increment;
		return new StringBuilder(_stock).append(_increment).toString();
	}
}