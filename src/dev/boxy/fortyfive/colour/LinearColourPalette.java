package dev.boxy.fortyfive.colour;

import java.util.*;

import dev.boxy.fortyfive.*;


public class LinearColourPalette implements ColourPalette {
	
	protected int idx;
	protected Colour current;
	
	String name;
	protected List<ColourFactory> colourFactories;
	
	public LinearColourPalette(String name, List<ColourFactory> colourFactories) {
		this.name = name;
		this.colourFactories = colourFactories;
		next();
	}
	
	public Colour current() {
		return current;
	}

	public Colour next() {
		int idx = FortyFive.ITERATIONS;
		
		if (idx >= colourFactories.size()) {
			idx = idx % colourFactories.size();
		}
		
		current = colourFactories.get(idx).get();
		return current;
	}
}
