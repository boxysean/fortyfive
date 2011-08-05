package dev.boxy.fortyfive.core.colour;

import java.util.*;

import dev.boxy.fortyfive.utils.*;

public class RandomColourPalette implements ColourPalette {
	
	protected int idx;
	protected Colour current;
	
	protected String name;
	protected List<Colour> colours;
	
	public RandomColourPalette(String name, List<Colour> colours) {
		this.name = name;
		this.colours = colours;
		next();
	}

	public Colour current() {
		return current;
	}

	public Colour next() {
		RandomSingleton rs = RandomSingleton.getInstance();
		Random rand = rs.get();
		
		idx = rand.nextInt(colours.size());
		current = colours.get(idx);
		
		return current;
	}
	
	public String getName() {
		return name;
	}
	
}
