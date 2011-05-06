package dev.boxy.fortyfive.colour;

import java.util.*;

import dev.boxy.fortyfive.*;

public abstract class ColourPalette {
	
	static int COLOUR_PALETTE_ID = 0;
	public static Map<String, ColourPalette> map = new HashMap<String, ColourPalette>();

	List<Colour> colours;
	Colour currentColour;
	int idx = 0;
	
	public ColourPalette() {
		
	}
	
	public ColourPalette(List<Colour> colours) {
		this();
		this.colours = colours;
		this.currentColour = colours.get(idx);
	}
	
	public Colour getColour() {
		return currentColour;
	}
	
	public int getRed() {
		return currentColour.red;
	}
	
	public int getGreen() {
		return currentColour.green;
	}
	
	public int getBlue() {
		return currentColour.blue;
	}
	
	// TODO constructor class?
	public static void init(Map<String, Object> masterMap) {
		List<Map<String, Object>> colourPaletteList = (List<Map<String, Object>>) masterMap.get("colourPalettes");
		
		if (colourPaletteList == null) {
			return;
		}
		
		for (Map<String, Object> map : colourPaletteList) {
			String name = ConfigParser.getString(map, "name", String.format("default_%05d", COLOUR_PALETTE_ID++));
			String mode = ConfigParser.getString(map, "mode", "random").toLowerCase();
			
			List<String> colourNames = (List<String>) map.get("colours");
			
			List<Colour> colours = new ArrayList<Colour>();
			
			for (String colourName : colourNames) {
				Colour colour = Colour.get(colourName);
				
				if (colour == null) {
					System.err.printf("ColourPalette init warning: colour name %s (palette %s) does not exist, skipping\n", colourName, name); 
				} else {
					colours.add(colour);
				}
			}
			
			if (colours.size() == 0) {
				System.err.printf("ColourPalette init warning: colour palette %s is empty\n", name); 
			}
			
			ColourPalette palette = null; 
			
			if (mode.startsWith("random")) {
				palette = new RandomColourPalette(colours);
			} else if (mode.startsWith("linear")) {
				palette = new LinearColourPalette(colours);
			}
			
			add(name, palette);
		}
	}
	
	public static void add(String name, ColourPalette colourPalette) {
		map.put(name, colourPalette);
	}

	public static ColourPalette get(String name) {
		return map.get(name);
	}

	public static ColourPalette getDefault() {
		if (!map.containsKey("default")) {
			Colour colour = Colour.getDefault();
			map.put("default", colour);
			return colour;
		} else {
			return map.get("default");
		}
	}
	
}
