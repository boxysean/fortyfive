package dev.boxy.fortyfive.core.colour;

import java.util.*;

import dev.boxy.fortyfive.*;


public class LinearColourPalette implements ColourPalette {
	
	protected int idx;
	protected Colour current;
	
	protected String name;
	protected List<Colour> colours;
	
	public LinearColourPalette(String name, List<Colour> colours) {
		this.name = name;
		this.colours = colours;
		next();
	}
	
	public Colour current() {
		return current;
	}

	public Colour next() {
		int idx = FortyFive.ITERATIONS;
		
		if (idx >= colours.size()) {
			idx = idx % colours.size();
		}
		
		current = colours.get(idx);
		return current;
	}
	
	public String getName() {
		return name;
	}
	
}
