package dev.boxy.fortyfive.core.draw;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.colour.*;

public class SolidDraw extends LineDraw {
	
	protected ColourPalette colourPalette;
	protected int strokeWidth;
	
	public SolidDraw(String name, ColourPalette colourPalette, int strokeWidth, String strokeJoinStr, String strokeCapStr) {
		super(name, strokeJoinStr, strokeCapStr);
		
		this.colourPalette = colourPalette;
		this.strokeWidth = strokeWidth;
	}
	
	public void drawLine(FortyFive ff, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy) {
		ff.strokeWeight(strokeWidth);
		ff.strokeCap(strokeCap);
		ff.strokeJoin(strokeJoin);
		
		Colour colour = colourPalette.current();
		
		ff.stroke(colour.red, colour.green, colour.blue);
		ff.line(px, py, pxx, pyy);
	}
	
}
