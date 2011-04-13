package dev.boxy.fortyfive.draw;

import processing.core.*;
import dev.boxy.fortyfive.*;

public class ImageDraw implements LineDraw {
	
	ImageGrid grid;
	int strokeWidth;
	
	public ImageDraw(ImageGrid grid, int strokeWidth) {
		this.grid = grid;
		this.strokeWidth = strokeWidth;
	}
	
	public void drawLine(FortyFive ff, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy) {
		ff.strokeWeight(strokeWidth);
		
		float pxm = (px + pxx) / 2.0f;
		float pym = (py + pyy) / 2.0f;
		ff.stroke(grid.at(gr, gc));
		ff.line(px, py, pxm, pym);
		ff.stroke(grid.at(grr, gcc));
		ff.line(pxm, pym, pxx, pyy);
	}
	
}
