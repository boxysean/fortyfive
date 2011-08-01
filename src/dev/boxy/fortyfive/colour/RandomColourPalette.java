package dev.boxy.fortyfive.colour;

import java.util.*;

import dev.boxy.fortyfive.*;

public class RandomColourPalette implements ColourPalette {
	
	protected int idx;
	protected Colour current;
	
	protected String name;
	protected List<ColourFactory> colourFactories;
	
	public RandomColourPalette(String name, List<ColourFactory> colourFactories) {
		this.name = name;
		this.colourFactories = colourFactories;
		next();
	}

	public Colour current() {
		return current;
	}

	public Colour next() {
		RandomSingleton rs = RandomSingleton.getInstance();
		Random rand = rs.get();
		
		idx = rand.nextInt(colourFactories.size());
		current = colourFactories.get(idx).get();
		
		return current;
	}
	
}
