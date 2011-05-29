package dev.boxy.fortyfive;

import java.util.*;

import dev.boxy.fortyfive.coordinatebag.*;


public class StartArea {
	
	protected FortyFive ff;
	
	// Lists of remaining places available.
	
	List<Coordinate>	coords		= new ArrayList<Coordinate>();
	Set<Coordinate>		coordsInit	= new HashSet<Coordinate>();
	CoordinateBag		coordBag	= null;
	
	boolean coordsCommitted = false;
	
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
		// Can take a coordinate only after the coordinate set has been compiled and converted into a list
		assert (coordsCommitted);
		
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
		
		public int hashCode() {
			return (r << 8) + c; 
		}
	}
	
	/**
	 * Add or remove a rectangle to the start area
	 * @param x left-most x coordinate
	 * @param y top-most y coordinate
	 * @param width width of rectangle
	 * @param height height of rectangle
	 */
	protected void rectangleAction(int x, int y, int width, int height, boolean add) {
		int gr = ff.pixelToGrid(y, ff.height, ff.rows());
		int gc = ff.pixelToGrid(x, ff.width, ff.columns());
		int grr = ff.pixelToGrid(y + height, ff.height, ff.rows());
		int gcc = ff.pixelToGrid(x + width, ff.width, ff.columns());
		
		int sr = Math.max(gr, 0);
		int sc = Math.max(gc, 0);
		int er = Math.min(grr, ff.rows());
		int ec = Math.min(gcc, ff.columns());
		
		for (int r = sr; r < er; r++) {
			for (int c = sc; c < ec; c++) {
				if (add) {
					coordsInit.add(new Coordinate(r, c));
				} else {
					coordsInit.remove(new Coordinate(r, c));
				}
			}
		}
		
		coordsCommitted = false;
	}
	
	/**
	 * Add a rectangle to the start area
	 * @param x left-most x coordinate
	 * @param y top-most y coordinate
	 * @param width width of rectangle
	 * @param height height of rectangle
	 */
	public void addRectangle(int x, int y, int width, int height) {
		rectangleAction(x, y, width, height, true);
	}
	
	/**
	 * Add a rectangle from the start area
	 * @param x left-most x coordinate
	 * @param y top-most y coordinate
	 * @param width width of rectangle
	 * @param height height of rectangle
	 */
	public void removeRectangle(int x, int y, int width, int height) {
		rectangleAction(x, y, width, height, false);
	}
	
	/**
	 * When finished initializing phase, invoke this method to commit the pre-initialized coordinates into a list
	 */
	public void commitCoords(List<ImageThreshold> thresholds) {
		// Remove all off-limits threshold coordinates
		
		int rows = ff.rows();
		int columns = ff.columns();
		
		boolean[][] blocked = new boolean[rows][columns];
		
		for (ImageThreshold threshold : thresholds) {
			threshold.apply(blocked);
		}
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				if (blocked[r][c]) {
					coordsInit.remove(new Coordinate(r, c));
				}
			}
		}
		
		// Transfer to list form
		
		coords.clear();
		coords.addAll(coordsInit);
		coordsInit.clear();
		
		// DEBUG
		
		if (FortyFive.SHOW_STARTAREA && coordsInit.size() != rows * columns) {
			for (Coordinate coord : coords) {
				int x = (int) ff.columnToX(coord.c);
				int y = (int) ff.rowToY(coord.r);
				
	            ff.fill(0, 255, 255, 30);
	            ff.noStroke();
	            ff.rect(x, y, ff.widthSpacing, ff.heightSpacing);
	        }
		}
		
		// Order the coordinates in a pre-defined way
		
		coordBag.initList(coords);
		
		coordsCommitted = true;
	}
}
