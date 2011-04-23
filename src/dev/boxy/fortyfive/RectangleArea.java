package dev.boxy.fortyfive;

import dev.boxy.fortyfive.coordinatebag.*;



public class RectangleArea extends StartArea {
	
	int x;
	int y;
	
	int width;
	int height;
	
	public RectangleArea(FortyFive ff, CoordinateBag coordBag, int x, int y, int width, int height) {
		super(ff, coordBag);
		
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
		
		initList();
	}
	
	public void initList() {
		int gr = ff.pixelToGrid(y, ff.height, ff.rows());
		int gc = ff.pixelToGrid(x, ff.width, ff.columns());
		int grr = ff.pixelToGrid(y + height, ff.height, ff.rows());
		int gcc = ff.pixelToGrid(x + width, ff.width, ff.columns());
		
		int sr = Math.max(gr, 0);
		int sc = Math.max(gc, 0);
		int er = Math.min(grr, ff.rows());
		int ec = Math.min(gcc, ff.columns());
		
		coords.clear();
		
		for (int r = sr; r < er; r++) {
			for (int c = sc; c < ec; c++) {
				coords.add(new Coordinate(r, c));
			}
		}
		
		coordBag.initList(coords);
	}

}
