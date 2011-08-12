package dev.boxy.fortyfive.core.startarea;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;

public class StartAreaFactory {
	
	protected String name;
	protected SceneFactory sceneFactory;
	protected List<Coordinate> coords;
	
	protected GridLayer gridLayer;
	
	public StartAreaFactory(String name, SceneFactory sceneFactory, List<Coordinate> coords) {
		this.name = name;
		this.sceneFactory = sceneFactory;
		this.coords = coords;
		
		makeGridLayer();
	}
	
	public StartArea get(Scene scene) {
		return new StartArea(scene, name, new ArrayList<Coordinate>(coords), gridLayer);
	}
	
	protected void makeGridLayer() {
		boolean[][] grid = new boolean[sceneFactory.rows()][sceneFactory.columns()];
		
		for (Coordinate coord : coords) {
			grid[coord.r][coord.c] = true;
		}
		
		gridLayer = new GridLayer(sceneFactory, grid, 0, 0);
		gridLayer.setName("start area " + name);
	}
	
	public String getName() {
		return name;
	}

}