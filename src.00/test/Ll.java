package test;

import org.finikes.tridge.log.Logger;

public class Ll extends Logger {

	@Override
	public void log0(Object... logs) {
		System.err.println(logs[0]);
	}

}
