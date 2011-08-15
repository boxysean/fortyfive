package dev.boxy.fortyfive.core.startarea;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;

public class StartAreaFactory {
	
	protected SceneFactory sceneFactory;
	protected List<Coordinate> coords;
	
	protected GridLayer gridLayer;
	
	public StartAreaFactory(SceneFactory sceneFactory, List<Coordinate> coords) {
		this.sceneFactory = sceneFactory;
		this.coords = coords;
	}
	
	public StartArea get() {
		return new StartArea(this, new ArrayList<Coordinate>(coords));
	}
	
	protected void makeGridLayer() {
		boolean[][] grid = new boolean[sceneFactory.rows()][sceneFactory.columns()];
		
		for (boolean[] g : grid) {
			Arrays.fill(g, true);
		}
		
		for (Coordinate coord : coords) {
			grid[coord.r][coord.c] = false;
		}
		
		gridLayer = new GridLayer(sceneFactory, grid, 0, 0);
		gridLayer.setName("start area");
	}
	
	public GridLayer getGridLayer() {
		if (gridLayer == null) {
			makeGridLayer();
		}
		
		return gridLayer;
	}
	
}