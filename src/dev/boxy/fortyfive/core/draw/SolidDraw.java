package dev.boxy.fortyfive.core.draw;

import processing.core.*;
import dev.boxy.fortyfive.core.colour.*;

public class SolidDraw implements LineDraw {
	
	protected String name;
	protected ColourPalette colourPalette;
	protected int strokeWidth;
	
	public SolidDraw(String name, ColourPalette colourPalette, int strokeWidth) {
		this.name = name;
		this.colourPalette = colourPalette;
		this.strokeWidth = strokeWidth;
	}
	
	public void drawLine(PGraphics pg, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy) {
		pg.beginDraw();
		
		pg.strokeWeight(strokeWidth);
		
		Colour colour = colourPalette.current();
		
		pg.stroke(colour.red, colour.green, colour.blue);
		pg.line(px, py, pxx, pyy);
		
		pg.endDraw();
	}
	
	public String getName() {
		return name;
	}
	
}
