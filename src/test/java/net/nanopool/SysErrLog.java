package net.nanopool;

import java.util.Date;

public class SysErrLog implements Log {
	private String name = "Log";
	
	private String now() {
		return (new Date()).toString();
	}
	
	public void debug(CharSequence mesg, Object... args) {
		System.err.printf(now() + "DEBUG " + name + " " + mesg + "\n", args);
	}

	public void debug(Throwable th, CharSequence mesg, Object... args) {
		System.err.printf(now() + "DEBUG " + name + " " + mesg + "\n", args);
		th.printStackTrace();
	}

	public void error(CharSequence mesg, Object... args) {
		System.err.printf(now() + "ERROR " + name + " " + mesg + "\n", args);
	}

	public void error(Throwable th, CharSequence mesg, Object... args) {
		System.err.printf(now() + "ERROR " + name + " " + mesg + "\n", args);
		th.printStackTrace();
	}

	public void info(CharSequence mesg, Object... args) {
		System.err.printf(now() + "INFO " + name + " " + mesg + "\n", args);
	}

	public void info(Throwable th, CharSequence mesg, Object... args) {
		System.err.printf(now() + "INFO " + name + " " + mesg + "\n", args);
		th.printStackTrace();
	}

	public Log subLoggerFor(CharSequence objectName) {
		SysErrLog log = new SysErrLog();
		log.name += "/" + objectName;
		return log;
	}

	public void warn(CharSequence mesg, Object... args) {
		System.err.printf(now() + "WARN " + name + " " + mesg + "\n", args);
	}

	public void warn(Throwable th, CharSequence mesg, Object... args) {
		System.err.printf(now() + "WARN " + name + " " + mesg + "\n", args);
		th.printStackTrace();
	}
}
