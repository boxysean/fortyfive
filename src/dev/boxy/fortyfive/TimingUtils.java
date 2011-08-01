package dev.boxy.fortyfive;

import java.util.*;

public class TimingUtils {
	
	public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("TIMING", "false"));
	
	public static Map<String, Long> marks = new HashMap<String, Long>();
	public static Map<String, Long> markAddMap = new HashMap<String, Long>();
	
	public static Set<String> markAddTags = new HashSet<String>();
	
	protected static Logger logger = Logger.getInstance();
	
	public static void mark(String tag) {
		if (!isEnabled()) {
			return;
		}
		
		if (marks.containsKey(tag)) {
			long time = System.currentTimeMillis();
			print(tag, time - marks.get(tag));
			marks.remove(tag);
		} else {
			marks.put(tag, System.currentTimeMillis());
		}
	}
	
	public static void markAdd(String tag) {
		if (!isEnabled()) {
			return;
		}
		
		long current = System.currentTimeMillis();
		
		if (markAddMap.containsKey(tag)) {
			long mark = 0;
			
			if (marks.containsKey(tag)) {
				mark = marks.get(tag);
			}
			
			long markAdd = current - markAddMap.get(tag);
			mark += markAdd;
			
			marks.put(tag, mark);
			markAddMap.remove(tag);
		} else {
			markAddMap.put(tag, current);
		}
		
		markAddTags.add(tag);
	}
	
	public static void print(String tag) {
		if (!isEnabled()) {
			return;
		}
		
		Long time = marks.get(tag);
		
		if (time == null) {
			return;
		}
		
		print(tag, time);
	}
	
	public static void print(String tag, long millis) {
		String prepend = "";
		
		if (markAddTags.contains(tag)) {
			prepend = "  ";
		}
		
		logger.timing("%s- %s: %.3f", prepend, tag, millis / 1000.0);
	}
	
	public static void reset() {
		marks.clear();
		markAddMap.clear();
		markAddTags.clear();
	}
	
	public static boolean isEnabled() {
		return ENABLED;
	}
	
}
