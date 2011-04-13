package dev.boxy.fortyfive;


public class RectangleArea extends StartArea {
	
	int x;
	int y;
	
	int width;
	int height;
	
	public RectangleArea(FortyFive ff, int x, int y, int width, int height) {
		super(ff);
		
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
		
		initLists();
	}
	
	public void initLists() {
		int gr = ff.pixelToGrid(y, ff.height, ff.rows());
		int gc = ff.pixelToGrid(x, ff.width, ff.columns());
		int grr = ff.pixelToGrid(y + height, ff.height, ff.rows());
		int gcc = ff.pixelToGrid(x + width, ff.width, ff.columns());
		
		for (int r = Math.max(gr, 0); r < Math.min(grr, ff.rows()); r++) {
			for (int c = Math.max(gc, 0); c < Math.min(gcc, ff.columns()); c++) {
				rrem.add(r);
				crem.add(c);
			}
		}
	}

}
