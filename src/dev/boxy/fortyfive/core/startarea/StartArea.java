package dev.boxy.fortyfive.core.startarea;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;


public class StartArea {
	
	protected StartAreaFactory startAreaFactory;
	protected List<Coordinate> coords = new ArrayList<Coordinate>(); // remaining available coordinates
	
	protected int gr;
	protected int gc;
	protected int idx;

	public StartArea(StartAreaFactory startAreaFactory, List<Coordinate> coords) {
		this.startAreaFactory = startAreaFactory;
		this.coords = coords;
	}
	
	/**
	 * Finds the next start point
	 * @return true if there is a point at which lines can start, false otherwise 
	 */
	public boolean getNextStartPoint(Scene scene) {
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
	
	public GridLayer getGridLayer() {
		return startAreaFactory.getGridLayer();
	}
	
}
