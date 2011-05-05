package dev.boxy.fortyfive;

import java.util.*;

import dev.boxy.fortyfive.coordinatebag.*;


public class StartArea {
	
	protected FortyFive ff;
	
	// Lists of remaining places available.
	
	List<Coordinate>	coords		= new ArrayList<Coordinate>();
	Set<Coordinate>		coordsInit	= new TreeSet<Coordinate>();
	CoordinateBag		coordBag	= null;
	
	static Map<CacheEntry, List<Coordinate>> coordsCache = new HashMap<CacheEntry, List<Coordinate>>();
	
	List<StartAreaShape>		shapeList	= new ArrayList<StartAreaShape>();
	
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
//		rectangleAction(x, y, width, height, true);
		shapeList.add(new Rectangle(x, y, width, height, true));
	}
	
	/**
	 * Add a rectangle from the start area
	 * @param x left-most x coordinate
	 * @param y top-most y coordinate
	 * @param width width of rectangle
	 * @param height height of rectangle
	 */
	public void removeRectangle(int x, int y, int width, int height) {
//		rectangleAction(x, y, width, height, false);
		shapeList.add(new Rectangle(x, y, width, height, false));
	}
	
	public void addThreshold(ImageThreshold threshold) {
		shapeList.add(new Threshold(threshold, true));
	}
	
	public void removeThreshold(ImageThreshold threshold) {
		shapeList.add(new Threshold(threshold, false));
	}
	
	/**
	 * When finished initializing phase, invoke this method to commit the pre-initialized coordinates into a list.
	 * If this combination of coordinates was previously computed, then dump the coordinate list into a new instance.
	 */
	public void commitCoords(List<ImageThreshold> thresholds) {
		for (ImageThreshold threshold : thresholds) {
			addThreshold(threshold);
		}
		
		Collections.sort(shapeList);
		
		// Check if this combination of rectangles has been cached
		
		CacheEntry ce = new CacheEntry(coordBag, shapeList);
		
		if (coordsCache.containsKey(ce)) {
			// It has been cached, use the pre-existing computation
			
			coords.addAll(coordsCache.get(ce));
			coordsCommitted = true;
		} else {
			// It has not been cached, compute the area and commit it to the cache
			
			int rows = ff.rows();
			int columns = ff.columns();
			
			boolean[][] blocked = new boolean[rows][columns];
			
			// Apply all previously defined rectangles and thresholds
			
			for (StartAreaShape shape : shapeList) {
				shape.apply(blocked); // TODO is this really the best way? can you forego the use of the 2D grid within this function?
			}
			
			// Remove all off-limits threshold coordinates

			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < columns; c++) {
					if (blocked[r][c]) {
						coordsInit.remove(new Coordinate(r, c));
					}
				}
			}
			
			// Transfer to a list rather than set
			
			List<Coordinate> cachedCoordinates = new ArrayList<Coordinate>();
			cachedCoordinates.addAll(coordsInit);
			coordsInit.clear();
			
			// DEBUG
			
			if (FortyFive.SHOW_STARTAREA && coords.size() != rows * columns) {
				for (Coordinate coord : coords) {
					int x = (int) ff.columnToX(coord.c);
					int y = (int) ff.rowToY(coord.r);
					
		            ff.fill(0, 255, 255, 30);
		            ff.noStroke();
		            ff.rect(x, y, ff.widthSpacing, ff.heightSpacing);
		        }
			}
			
			// Order the coordinates in a pre-defined way
			
			coordBag.initList(cachedCoordinates);
			
			// Commit to cache
			
			coordsCache.put(ce, cachedCoordinates);
			
			// Commit to current working copy
			
			coords.addAll(cachedCoordinates);
			coordsCommitted = true;
		}
	}
	
	
	static abstract class StartAreaShape implements Comparable<StartAreaShape> {
		static int MASTER_ID = 0;
		
		int id;
		
		public StartAreaShape() {
			this.id = MASTER_ID++;
		}
		
		public int compareTo(StartAreaShape s) {
			return id - s.id;
		}
		
		abstract void apply(boolean[][] blocked);
	}
	
	class Threshold extends StartAreaShape {
		boolean add;
		
		ImageThreshold threshold;
		
		public Threshold(ImageThreshold threshold, boolean add) {
			super();
			
			this.threshold = threshold;
			this.add = add;
		}
		
		@Override
		public boolean equals(Object o) {
			try {
				Threshold t = (Threshold) o;
				
				if (!t.threshold.equals(threshold)) {
					return false;
				} else if (t.add != add) {
					return false;
				} else {
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			int res = threshold.name.hashCode();
			res = (res * 2) + (add ? 0 : 1);
			return res;
		}

		@Override
		void apply(boolean[][] blocked) {
			threshold.apply(blocked);
		}
	}
	
	class Rectangle extends StartAreaShape {
		boolean add;
		
		int x;
		int y;
		int width;
		int height;
		
		public Rectangle(int x, int y, int width, int height, boolean add) {
			super();
			
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.add = add;
		}
		
		@Override
		public boolean equals(Object o) {
			try {
				Rectangle r = (Rectangle) o;
				
				if (r.x != x) {
					return false;
				} else if (r.y != y) {
					return false;
				} else if (r.width != width) {
					return false;
				} else if (r.height != height) {
					return false;
				} else if (r.add != add) {
					return false;
				} else {
					return true;
				}
			} catch (Exception e) {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			int res = x;
			res = (res * 31) + y;
			res = (res * 31) + width;
			res = (res * 31) + height;
			res = (res * 2) + (add ? 0 : 1);
			return res;
		}

		@Override
		void apply(boolean[][] blocked) {
			rectangleAction(x, y, width, height, add);
		}
	}
	
	class CacheEntry {
		CoordinateBag coordBag;
		List<StartAreaShape> shapeList;
		
		public CacheEntry(CoordinateBag coordBag, List<StartAreaShape> shapeList) {
			this.coordBag = coordBag;
			this.shapeList = shapeList;
		}
		
		@Override
		public int hashCode() {
			return coordBag.hashCode() * 31 + shapeList.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			try {
				CacheEntry ce = (CacheEntry) o;
				return coordBag.equals(ce.coordBag) && shapeList.equals(ce.shapeList);
			} catch (Exception e) {
				return false;
			}
		}
	}
}
