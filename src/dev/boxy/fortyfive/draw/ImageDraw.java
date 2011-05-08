package dev.boxy.fortyfive.draw;

import dev.boxy.fortyfive.*;

public class ImageDraw extends LineDraw {
	
	ImageGrid	grid;
	int 		strokeWidth;
	
	int			xOffset;
	int			yOffset;
	double		scale;
	
	public ImageDraw(ImageGrid grid, int strokeWidth, int xOffset, int yOffset, double scale, String strokeJoinStr, String strokeCapStr) {
		super(strokeJoinStr, strokeCapStr);
		
		this.grid = grid;
		this.strokeWidth = strokeWidth;
		
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.scale = scale;
	}
	
	public void drawLine(FortyFive ff, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy) {
		ff.strokeWeight(strokeWidth);
		ff.strokeCap(strokeCap);
		ff.strokeJoin(strokeJoin);
		
		float pxm = (px + pxx) / 2.0f;
		float pym = (py + pyy) / 2.0f;
		
		int cgr = ff.yToRow(ff.rowToY(gr) - yOffset);
		int cgc = ff.xToColumn(ff.columnToX(gc) - xOffset);
		
		int cgrr = ff.yToRow(ff.rowToY(gr) - yOffset);
		int cgcc = ff.xToColumn(ff.columnToX(gc) - xOffset);
		
		ff.stroke(grid.colourAt(cgr, cgc, scale));
		ff.line(px, py, pxm, pym);
		ff.stroke(grid.colourAt(cgrr, cgcc, scale));
		ff.line(pxm, pym, pxx, pyy);
	}

	public void onComplete() {
		
	}
	
}
