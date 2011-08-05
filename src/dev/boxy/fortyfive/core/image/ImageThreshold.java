package dev.boxy.fortyfive.core.image;

import dev.boxy.fortyfive.core.scene.*;


public class ImageThreshold {
	
	protected SceneFactory sceneFactory;
	
	protected String name;
	protected String imageName;
	protected boolean invert;
	protected int xOffset;
	protected int yOffset;
	protected double scale;
	
	public ImageThreshold(SceneFactory sceneFactory, String name, String imageName, boolean invert, int xOffset, int yOffset, double scale) {
		this.sceneFactory = sceneFactory;
		this.name = name;
		this.imageName = imageName;
		this.invert = invert;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.scale = scale;
	}
	
	public void apply(boolean[][] blocked) {
		ImageGrid image = sceneFactory.getImageGrid(imageName);
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
		ImageGrid image = sceneFactory.getImageGrid(imageName);
		return image.getWidth() * scale;
	}
	
	public double getHeight() {
		ImageGrid image = sceneFactory.getImageGrid(imageName);
		return image.getHeight() * scale;
	}
	
}
