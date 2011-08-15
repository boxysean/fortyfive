package dev.boxy.fortyfive.utils;

import java.util.*;

public class ConfigParser {

	public static int getInt(Map<String, Object> map, String key) {
		Integer i = (Integer) map.get(key);
		return i.intValue();
	}
	
	public static int getInt(Map<String, Object> map, String key, int def) {
		if (map.containsKey(key)) {
			return getInt(map, key);
		} else {
			return def;
		}
	}
	
	public static int getInt(List<Object> list, int idx, int def) {
		if (list.size() <= idx) {
			return def;
		} else {
			return ((Integer) list.get(idx)).intValue();
		}
	}
	
	public static double getDouble(Map<String, Object> map, String key) {
		Object o = map.get(key);
		
		if (o instanceof Integer) {
			Integer d = (Integer) o;
			return d.intValue();
		} else {
			Double d = (Double) map.get(key);
			return d.doubleValue();
		}
	}
	
	public static double getDouble(Map<String, Object> map, String key, double def) {
		if (map.containsKey(key)) {
			return getDouble(map, key);
		} else {
			return def;
		}
	}
	
	public static boolean getBoolean(Map<String, Object> map, String key) {
		Boolean b = (Boolean) map.get(key);
		return b.booleanValue();
	}
	
	public static boolean getBoolean(Map<String, Object> map, String key, boolean def) {
		if (map.containsKey(key)) {
			return getBoolean(map, key);
		} else {
			return def;
		}
	}
	
	public static boolean getBoolean(Map<String, Object> map, String key, String[] matches) {
		Object v = map.get(key);
		
		if (v == null) {
			return false;
		} else if (v instanceof Boolean) {
			return ((Boolean) v).booleanValue();
		}
		
		String value = getString(map, key);
		
		for (String match : matches) {
			if (match.equalsIgnoreCase(value)) {
				return true;
			}
		}
		
		return Boolean.parseBoolean(value);
	}
	
	public static String getString(Map<String, Object> map, String key, String def) {
		if (map.containsKey(key)) {
			return getString(map, key);
		} else {
			return def;
		}
	}
	
	public static String getString(Map<String, Object> map, String key) {
		return (String) map.get(key);
	}
	
	public static String getString(Map<String, Object> map, String[] keys, String def) {
		for (String key : keys) {
			if (map.containsKey(key)) {
				return (String) map.get(key);
			}
		}
		
		return def;
	}
	
	public static String getString(Map<String, Object> map, String[] keys) {
		for (String key : keys) {
			if (map.containsKey(key)) {
				return (String) map.get(key);
			}
		}
		
		return null;
	}
	
	public static List<String> getStrings(Map<String, Object> map, String key) {
		ArrayList<String> res = new ArrayList<String>();
		
		Object o = map.get(key);
		
		if (o == null) {
			return null;
		}
		
		if (o instanceof List<?>) {
			List<String> l = (List<String>) o;
			
			for (String s : l) {
				res.add(s);
			}
		} else if (o instanceof String) {
			res.add((String) o);
		} else {
			throw new Error("getStrings: unknown type");
		}
		
		return res;
	}
	
	public static int parseInt(List<Object> list, int idx, int n, int def) {
		return parseInt(list.get(idx), n, def);
	}
		
	public static int parseInt(Map<String, Object> map, String key, int n, int def) {
		return parseInt(map.get(key), n, def);
	}
		
	public static int parseInt(Object xObj, int n, int def) {
		try {
			int x = 0;
			
			if (xObj instanceof Integer) {
				x = ((Integer) xObj).intValue();
			} else if (xObj instanceof String) {
				String xStr = (String) xObj;
				
				if (xStr.matches("^-?[0-9]*\\/\\s*-?[0-9]\\+$")) {
					// If it's any ratio form, then multiply the ratio by the length / width / etc
					
					String tokens[] = xStr.split("/");
					
					int numerator;
					int denominator;
					
					if (tokens.length == 1) {
						numerator = 1;
						denominator = Integer.parseInt(tokens[0]);
					} else {
						numerator = Integer.parseInt(tokens[0]);
						denominator = Integer.parseInt(tokens[1]);
					}
					
					x = (int) ((double) numerator / denominator * n);
				} else {
					x = Integer.parseInt(xStr);
				}
			}
			
			if (x < 0) {
				x += n;
			}
			
			return x;
		} catch (Exception e) {
			return def;
		}
	}
	
}
