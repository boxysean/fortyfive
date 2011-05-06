package dev.boxy.fortyfive.colour;

import java.util.*;

public class RandomColourPalette extends ColourPalette {
	
	static Random rand = new Random();
	
	public RandomColourPalette(List<Colour> colours) {
		super(colours);
		
		currentColour = colours.get(rand.nextInt(colours.size()));
	}
	
}
