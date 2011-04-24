package dev.boxy.fortyfive;

import java.util.*;

import dev.boxy.fortyfive.coordinatebag.*;


public abstract class StartArea {
	
	protected FortyFive ff;
	
	// Lists of remaining places available.
	
	List<Coordinate>	coords 		= new ArrayList<Coordinate>();
	
	CoordinateBag		coordBag	= null;
	
	int gr;
	int gc;
	
	int idx;
	
	public StartArea(FortyFive ff, CoordinateBag coordBag) {
		this.ff = ff;
		this.coordBag = coordBag;
	}
	
	/**
	 * Finds the next start point
	 * @return true if there is a point at which lines can start, false otherwise 
	 */
	public boolean getNextStartPoint(List<ImageThreshold> thresholds) {
		do {
			if (idx >= coords.size()) {
				gr = -1;
				gc = -1;
				return false;
			}
			
			Coordinate coord = coords.get(idx++);
			
			gr = coord.r;
			gc = coord.c;
		} while (ff.grid[gr][gc]);
		
		return true;
	}
	
	public int getStartRow() {
		return gr;
	}
	
	public int getStartColumn() {
		return gc;
	}
	
	public class Coordinate implements Comparable<Coordinate> {
		int r;
		int c;
		
		public Coordinate(int r, int c) {
			this.r = r;
			this.c = c;
		}

		public int compareTo(Coordinate o) {
			if (o.r < r) {
				return -1;
			} else if (o.r > r) {
				return 1;
			} else if (o.c < c) {
				return -1;
			} else if (o.c > c) {
				return 1;
			} else {
				return 0;
			}
		}
		
		public boolean equals(Object o) {
			try {
				Coordinate coord = (Coordinate) o;
				return coord.r == r && coord.c == c;
			} catch (Exception e) {
				return false;
			}
		}
	}
	
}
