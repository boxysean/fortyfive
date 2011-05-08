package dev.boxy.fortyfive.draw;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.colour.*;

public class SolidDraw extends LineDraw {
	
	ColourPalette colourPalette;
	int strokeWidth;
	
	public SolidDraw(ColourPalette colourPalette, int strokeWidth, String strokeJoinStr, String strokeCapStr) {
		super(strokeJoinStr, strokeCapStr);
		
		this.colourPalette = colourPalette;
		this.strokeWidth = strokeWidth;
	}
	
	public void drawLine(FortyFive ff, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy) {
		ff.strokeWeight(strokeWidth);
		ff.strokeCap(strokeCap);
		ff.strokeJoin(strokeJoin);
		ff.stroke(colourPalette.getRed(), colourPalette.getGreen(), colourPalette.getBlue());
		ff.line(px, py, pxx, pyy);
	}
	
}
