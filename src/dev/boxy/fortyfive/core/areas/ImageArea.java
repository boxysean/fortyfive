package dev.boxy.fortyfive.core.areas;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.image.*;
import dev.boxy.fortyfive.core.scene.*;


public class ImageArea implements Area {
	
	protected SceneFactory sceneFactory;
	
	protected String name;
	protected String imageName;
	protected int xOffset;
	protected int yOffset;
	protected double scale;
	
	/* debug show image threshold stuff */
	
	protected GridLayer gridLayer;
	
	public ImageArea(SceneFactory sceneFactory, String name, String imageName, int xOffset, int yOffset, double scale) {
		this.sceneFactory = sceneFactory;
		this.name = name;
		this.imageName = imageName;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.scale = scale;
	}
	
	public void add(boolean[][] blocked) {
		ImageGrid image = sceneFactory.getImageGrid(imageName);
		image.addThreshold(blocked, xOffset, yOffset, scale);
	}
	
	public void subtract(boolean[][] blocked) {
		ImageGrid image = sceneFactory.getImageGrid(imageName);
		image.subtractThreshold(blocked, xOffset, yOffset, scale);
	}
	
	public void set(boolean[][] blocked) {
		ImageGrid image = sceneFactory.getImageGrid(imageName);
		image.setThreshold(blocked, xOffset, yOffset, scale);
	}
	
	public void unset(boolean[][] blocked) {
		ImageGrid image = sceneFactory.getImageGrid(imageName);
		image.unsetThreshold(blocked, xOffset, yOffset, scale);
	}
	
	protected void makeGridLayer() {
		boolean[][] grid = new boolean[sceneFactory.rows()][sceneFactory.columns()];
		
		ImageGrid image = sceneFactory.getImageGrid(imageName);
		image.addThreshold(grid, xOffset, yOffset, scale);
		
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
		if (gridLayer == null) {
			makeGridLayer();
		}
		
		return gridLayer;
	}
	
}
