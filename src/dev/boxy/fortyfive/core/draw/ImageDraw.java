package dev.boxy.fortyfive.core.draw;

import processing.core.*;
import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.image.*;
import dev.boxy.fortyfive.core.scene.*;

public class ImageDraw extends LineDraw {
	
	protected Scene scene;
	
	protected ImageGrid grid;
	protected int strokeWidth;
	
	protected int xOffset;
	protected int yOffset;
	protected double scale;
	
	public ImageDraw(Scene scene, String name, ImageGrid grid, int strokeWidth, int theXOffset, int theYOffset, double theScale, String strokeJoinStr, String strokeCapStr) {
		super(name, strokeJoinStr, strokeCapStr);
		
		this.scene = scene;
		
		this.grid = grid;
		this.strokeWidth = strokeWidth;
		
		this.xOffset = theXOffset;
		this.yOffset = theYOffset;
		this.scale = theScale;
		
		FortyFive ff = FortyFive.getInstance();
		
		if (scale < 0) {
			int imageWidth = grid.getWidth();
			int imageHeight = grid.getHeight();
			
			double widthResizeRatio = (double) ff.getWidth() / imageWidth;
			double heightResizeRatio = (double) ff.getHeight() / imageHeight;
			
			if (scale < -5) {
				scale = Math.max(widthResizeRatio, heightResizeRatio);
			} else {
				scale = Math.min(widthResizeRatio, heightResizeRatio);
			}
		}
		
		if (xOffset < 0) {
			// Center the image along x
			xOffset = (int) (ff.getWidth() - (grid.getWidth() * scale)) / 2;
		}
		
		if (yOffset < 0) {
			// Center the image along y
			yOffset = (int) (ff.getHeight() - (grid.getHeight() * scale)) / 2;
		}
	}
	
	public void drawLine(PGraphics g, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy) {
		g.beginDraw();
		
		g.strokeWeight(strokeWidth);
		g.strokeCap(strokeCap);
		g.strokeJoin(strokeJoin);
		
		float pxm = (px + pxx) / 2.0f;
		float pym = (py + pyy) / 2.0f;
		
		int cgr = scene.yToRow(scene.rowToY(gr) - yOffset);
		int cgc = scene.xToColumn(scene.columnToX(gc) - xOffset);
		
		int cgrr = scene.yToRow(scene.rowToY(gr) - yOffset);
		int cgcc = scene.xToColumn(scene.columnToX(gc) - xOffset);
		
		g.stroke(grid.colourAt(cgr, cgc, scale));
		g.line(px, py, pxm, pym);
		g.stroke(grid.colourAt(cgrr, cgcc, scale));
		g.line(pxm, pym, pxx, pyy);
		
		g.endDraw();
	}
	
}
