package test;

import org.finikes.tridge.log.Logger;

public class Ll extends Logger {

	@Override
	public void wrap(Object... logs) {
		System.err.println(logs[0]);
	}

}
