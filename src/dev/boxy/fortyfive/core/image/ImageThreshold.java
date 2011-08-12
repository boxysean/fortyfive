package dev.boxy.fortyfive.core.image;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;


public class ImageThreshold {
	
	protected SceneFactory sceneFactory;
	
	protected String name;
	protected String imageName;
	protected boolean invert;
	protected int xOffset;
	protected int yOffset;
	protected double scale;
	
	/* debug show image threshold stuff */
	
	protected GridLayer gridLayer;
	
	public ImageThreshold(SceneFactory sceneFactory, String name, String imageName, boolean invert, int xOffset, int yOffset, double scale) {
		this.sceneFactory = sceneFactory;
		this.name = name;
		this.imageName = imageName;
		this.invert = invert;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.scale = scale;
		
		makeGridLayer();
	}
	
	public void apply(boolean[][] blocked) {
		apply(blocked, ImageGrid.MODE_OR);
	}
	
	public void apply(boolean[][] blocked, int mode) {
		ImageGrid image = sceneFactory.getImageGrid(imageName);
		image.applyThreshold(blocked, invert, xOffset, yOffset, scale, mode);
	}
	
	protected void makeGridLayer() {
		boolean[][] grid = new boolean[sceneFactory.rows()][sceneFactory.columns()];
		
		ImageGrid image = sceneFactory.getImageGrid(imageName);
		image.applyThreshold(grid, invert, xOffset, yOffset, scale, ImageGrid.MODE_OR);
		
		gridLayer = new GridLayer(sceneFactory, grid, 0, 0);
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
	
	public GridLayer getGridLayer() {
		return gridLayer;
	}
	
}
