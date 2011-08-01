package dev.boxy.fortyfive.colour;

import java.util.*;

public class ColourPaletteFactoryMap {

	private static ColourPaletteFactoryMap INSTANCE;
	
	private ColourPaletteFactoryMap() {
		
	}
	
	public static ColourPaletteFactoryMap getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ColourPaletteFactoryMap();
		}
		
		return INSTANCE;
	}
	
	protected Map<String, ColourPaletteFactory> map = new HashMap<String, ColourPaletteFactory>();
	
	public void add(ColourPaletteFactory colourPaletteFactory) {
		map.put(colourPaletteFactory.getName(), colourPaletteFactory);
	}
	
	public ColourPaletteFactory get(String name) {
		return map.get(name);
	}
	
}
