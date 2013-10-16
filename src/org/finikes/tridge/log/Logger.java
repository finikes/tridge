package org.finikes.tridge.log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class Logger {
	private static final Logger LOGGER = v();

	public static final void log(Object... logs) {
		LOGGER.wrap(logs);
	}

	private static final Logger v() {
		InputStream in = null;
		Logger logger = null;
		try {
			in = Object.class.getResourceAsStream("/log.sys.properties");
			Properties p = new Properties();
			p.load(in);

			String className = p.getProperty("className");
			logger = (Logger) (Class.forName(className).newInstance());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return logger;
	}

	public abstract void wrap(Object... logs);
}
