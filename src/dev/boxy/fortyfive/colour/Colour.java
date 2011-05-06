package dev.boxy.fortyfive.colour;

import java.util.*;

import dev.boxy.fortyfive.*;

public class Colour extends ColourPalette {
	
	public static int COLOUR_ID = 0;
	public static Map<String, Colour> map = new HashMap<String, Colour>();
	
	public String name;
	public int red;
	public int green;
	public int blue;
	
	public Colour(String name, int red, int green, int blue) {
		this.name = name;
		this.red = red;
		this.green = green;
		this.blue = blue;
		
		currentColour = this;
	}
	
	
	// TODO should this go in a factory class?
	public static void init(Map<String, Object> masterMap) {
		List<Map<String, Object>> colourList = (List<Map<String, Object>>) masterMap.get("colours");
		
		if (colourList == null) {
			return;
		}

		for (Map<String, Object> map : colourList) {
			String name = ConfigParser.getString(map, "name", getDefaultName());
			int red = ConfigParser.getInt(map, "red", 0);
			int green = ConfigParser.getInt(map, "green", 0);
			int blue = ConfigParser.getInt(map, "blue", 0);
			
			add(name, new Colour(name, red, green, blue));
		}
	}
	
	public static Colour get(String colourName) {
		return map.get(colourName);
	}
	
	public static void add(String colourName, Colour colour) {
		map.put(colourName, colour);
	}
	
	public static String getDefaultName() {
		return String.format("default_%05d", COLOUR_ID++);
	}

	public static Colour getDefault() {
		if (!map.containsKey("default")) {
			Colour colour = new Colour("default", 0, 0, 0);
			map.put("default", colour);
			return colour;
		} else {
			return map.get("default");
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
