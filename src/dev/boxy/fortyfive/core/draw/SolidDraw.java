package dev.boxy.fortyfive.core.draw;

import processing.core.*;
import dev.boxy.fortyfive.core.colour.*;

public class SolidDraw extends LineDraw {
	
	protected ColourPalette colourPalette;
	protected int strokeWidth;
	
	public SolidDraw(String name, ColourPalette colourPalette, int strokeWidth, String strokeJoinStr, String strokeCapStr) {
		super(name, strokeJoinStr, strokeCapStr);
		
		this.colourPalette = colourPalette;
		this.strokeWidth = strokeWidth;
	}
	
	public void drawLine(PGraphics pg, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy) {
		pg.beginDraw();
		
		pg.strokeWeight(strokeWidth);
		pg.strokeCap(strokeCap);
		pg.strokeJoin(strokeJoin);
		
		Colour colour = colourPalette.current();
		
		pg.stroke(colour.red, colour.green, colour.blue);
		pg.line(px, py, pxx, pyy);
		
		pg.endDraw();
	}
	
}
