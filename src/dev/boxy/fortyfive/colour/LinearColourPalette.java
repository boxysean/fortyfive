package dev.boxy.fortyfive.colour;

import java.util.*;

import dev.boxy.fortyfive.*;


public class LinearColourPalette extends ColourPalette {
	
	public LinearColourPalette(List<Colour> colours) {
		super(colours);
		
		int idx = FortyFive.ITERATIONS;
		
		if (idx >= colours.size()) {
			idx = idx % colours.size();
		}
		
		currentColour = colours.get(idx);
	}
	
	@Override
	public Colour getColour() {
		return currentColour;
	}
}
