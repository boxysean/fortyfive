package dev.boxy.fortyfive.core.areas;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;


public class RectangleArea implements Area {
	
	protected SceneFactory sceneFactory;
	
	protected String name;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	
	/* debug show image threshold stuff */
	
	protected GridLayer gridLayer;
	
	public RectangleArea(SceneFactory sceneFactory, String name, int x, int y, int width, int height) {
		this.sceneFactory = sceneFactory;
		this.name = name;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	protected void action(boolean[][] grid, boolean action) {
		int sr = sceneFactory.yToRow(y);
		int er = sceneFactory.yToRow(y + height);
		int sc = sceneFactory.yToRow(x);
		int ec = sceneFactory.yToRow(x + width);
		
		for (int r = sr; r < er; r++) {
			for (int c = sc; c < ec; c++) {
				grid[r][c] = action;
			}
		}
	}
	
	public void set(boolean[][] grid) {
		action(grid, true);
	}
	
	public void unset(boolean[][] grid) {
		action(grid, false);
	}
	
	public void add(boolean[][] grid) {
		action(grid, true);
	}
	
	public void subtract(boolean[][] grid) {
		action(grid, false);
	}
	
	protected void makeGridLayer() {
		boolean[][] grid = new boolean[sceneFactory.rows()][sceneFactory.columns()];
		add(grid);
		
		gridLayer = new GridLayer(sceneFactory, grid, 0, 0);
	}

	public String getName() {
		return name;
	}
	
	public int getXOffset() {
		return x;
	}
	
	public int getYOffset() {
		return y;
	}
	
	public double getWidth() {
		return width;
	}
	
	public double getHeight() {
		return height;
	}
	
	public GridLayer getGridLayer() {
		if (gridLayer == null) {
			makeGridLayer();
		}
		
		return gridLayer;
	}
	
}
