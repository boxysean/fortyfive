package dev.boxy.fortyfive.colour;

import java.util.*;

public class ColourFactoryMap {

	public static ColourFactoryMap INSTANCE;
	
	public static ColourFactoryMap getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ColourFactoryMap();
		}
		
		return INSTANCE;
	}
	
	protected Map<String, ColourFactory> map = new HashMap<String, ColourFactory>();
	
	public void add(ColourFactory colourFactory) {
		map.put(colourFactory.getName(), colourFactory);
	}
	
	public ColourFactory get(String name) {
		return map.get(name);
	}
	
}
