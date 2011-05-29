package dev.boxy.fortyfive;



public class ImageThreshold {

	String name;
	ImageGrid image;
	boolean invert;
	int xOffset;
	int yOffset;
	double scale;
	
	public ImageThreshold(String name, ImageGrid image, boolean invert, int xOffset, int yOffset, double scale) {
		this.name = name;
		this.image = image;
		this.invert = invert;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.scale = scale;
	}
	
	public void apply(boolean[][] blocked) {
		image.applyThreshold(blocked, invert, xOffset, yOffset, scale);
	}
	
}
