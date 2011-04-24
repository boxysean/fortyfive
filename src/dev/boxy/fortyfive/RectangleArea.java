package dev.boxy.fortyfive;

import java.util.*;

import dev.boxy.fortyfive.coordinatebag.*;



public class RectangleArea extends StartArea {
	
	int x;
	int y;
	
	int width;
	int height;
	
	public RectangleArea(FortyFive ff, List<ImageThreshold> thresholds, CoordinateBag coordBag, int x, int y, int width, int height) {
		super(ff, coordBag);
		
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
		
		initList(thresholds);
	}
	
	public void initList(List<ImageThreshold> thresholds) {
		int gr = ff.pixelToGrid(y, ff.height, ff.rows());
		int gc = ff.pixelToGrid(x, ff.width, ff.columns());
		int grr = ff.pixelToGrid(y + height, ff.height, ff.rows());
		int gcc = ff.pixelToGrid(x + width, ff.width, ff.columns());
		
		int sr = Math.max(gr, 0);
		int sc = Math.max(gc, 0);
		int er = Math.min(grr, ff.rows());
		int ec = Math.min(gcc, ff.columns());
		
		if (FortyFive.SHOW_STARTAREA) {
			ff.fill(0, 255, 0, 30);
			ff.noStroke();
			ff.rect(x, y, width, height);
		}
		
		coords.clear();
		
		
		
		boolean[][] blocked = new boolean[ff.rows()][ff.columns()];
		
		if (thresholds != null) {
			// Check each threshold to make sure this starting point is okay.
			
			// TODO this is doing the same thing twice, maybe this can be organized better
			
			for (ImageThreshold threshold : thresholds) {
				threshold.apply(blocked);
			}
		}

		
		
		for (int r = sr; r < er; r++) {
			for (int c = sc; c < ec; c++) {
				if (!blocked[r][c]) {
					coords.add(new Coordinate(r, c));
				}
			}
		}
		
		coordBag.initList(coords);
	}

}
