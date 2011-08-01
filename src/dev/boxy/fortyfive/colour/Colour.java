package dev.boxy.fortyfive.colour;

public class Colour implements ColourPalette {
	
	public String name;
	public int red;
	public int green;
	public int blue;
	
	public Colour(String name, int red, int green, int blue) {
		this.name = name;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public Colour current() {
		return this;
	}

	public Colour next() {
		return this;
	}
	
}
