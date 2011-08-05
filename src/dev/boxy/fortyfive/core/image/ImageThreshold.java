package dev.boxy.fortyfive.core.image;


public class ImageThreshold {

	protected String name;
	protected ImageGrid image;
	protected boolean invert;
	protected int xOffset;
	protected int yOffset;
	protected double scale;
	
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
	
	public String getName() {
		return name;
	}
	
	public int getXOffset() {
		return xOffset;
	}
	
	public int getYOffset() {
		return yOffset;
	}
	
	public double getScale() {
		return scale;
	}
	
	public double getWidth() {
		return image.getWidth() * scale;
	}
	
	public double getHeight() {
		return image.getHeight() * scale;
	}
	
}
