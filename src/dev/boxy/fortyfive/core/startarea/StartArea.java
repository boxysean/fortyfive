package dev.boxy.fortyfive.core.startarea;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.image.*;
import dev.boxy.fortyfive.core.scene.*;


public class StartArea {
	
	protected Scene scene;
	
	protected String name;
	protected int gr;
	protected int gc;
	protected int idx;
	protected GridLayer gridLayer;
	
	protected List<Coordinate> coords = new ArrayList<Coordinate>(); // remaining available coordinates

	public StartArea(Scene scene, String name, List<Coordinate> coords, GridLayer gridLayer) {
		this.scene = scene;
		this.name = name;
		this.coords = coords;
		this.gridLayer = gridLayer;
	}
	
	/**
	 * Finds the next start point
	 * @return true if there is a point at which lines can start, false otherwise 
	 */
	public boolean getNextStartPoint(List<ImageThreshold> thresholds) {
		// TODO doesn't do anything with the thresholds...
		
		do {
			if (idx >= coords.size()) {
				gr = -1;
				gc = -1;
				return false;
			}
			
			Coordinate coord = coords.get(idx++);
			
			gr = coord.r;
			gc = coord.c;
		} while (scene.checkGrid(gr, gc));
		
		return true;
	}
	
	/**
	 * Retrieve the next start point row
	 * @return next start point row
	 */
	public int getStartRow() {
		return gr;
	}
	
	/**
	 * Retrieve the next start point column
	 * @return next start point column
	 */
	public int getStartColumn() {
		return gc;
	}
	
	public String getName() {
		return name;
	}
	
	public GridLayer getGridLayer() {
		return gridLayer;
	}
	
}
