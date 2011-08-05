package dev.boxy.fortyfive.utils;

public class Logger {
	
	protected static String PRE_PRINTF = "%12s ";
	protected static String TIMING_PRE = String.format(PRE_PRINTF, "timing ++");
	protected static String WARNING_PRE = String.format(PRE_PRINTF, "warning +++");
	protected static String ERROR_PRE = String.format(PRE_PRINTF, "error ++++");
	protected static String DEBUG_PRE = String.format(PRE_PRINTF, "debug +");
	protected static String LOG_PRE = String.format(PRE_PRINTF, "log ");
	
	private Logger() {
		
	}
	
	private static Logger INSTANCE;
	
	public static Logger getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Logger();
		}
		
		return INSTANCE;
	}
	
	public void log(String s) {
		System.err.println(LOG_PRE + s);
	}
	
	public void log(String s, Object... o) {
		System.err.printf(LOG_PRE + s + '\n', o);
	}
	
	public void timing(String s) {
		System.err.println(TIMING_PRE + s);
	}
	
	public void timing(String s, Object... o) {
		System.err.printf(TIMING_PRE + s + '\n', o);
	}
	
	public void debug(String s) {
		System.err.println(DEBUG_PRE + s);
	}
	
	public void debug(String s, Object... o) {
		System.err.printf(DEBUG_PRE + s + '\n', o);
	}
	
	public void warning(String s) {
		System.err.println(WARNING_PRE + s);
	}
	
	public void warning(String s, Object... o) {
		System.err.printf(WARNING_PRE + s + '\n', o);
	}
	
	public void error(String s) {
		System.err.println(ERROR_PRE + s);
	}
	
	public void error(String s, Object... o) {
		System.err.printf(ERROR_PRE + s + '\n', o);
	}
	
}
