package dev.boxy.fortyfive.core.draw;

import processing.core.*;
import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.colour.*;

public class SolidDraw implements LineDraw {
	
	protected String name;
	protected ColourPalette colourPalette;
	protected int strokeWidth;
	protected int strokeJoin;
	protected int strokeCap;
	
	public SolidDraw(String name, ColourPalette colourPalette, int strokeWidth, int strokeJoin, int strokeCap) {
		this.name = name;
		this.colourPalette = colourPalette;
		this.strokeWidth = strokeWidth;
		this.strokeJoin = strokeJoin;
		this.strokeCap = strokeCap;
	}
	
	public void drawLine(PGraphics pg, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy) {
		pg.beginDraw();
		
		pg.strokeWeight(strokeWidth);
		pg.strokeCap(strokeCap);
		pg.strokeJoin(strokeJoin);
		
		FortyFive ff = FortyFive.getInstance();
		
		ff.strokeCap(strokeCap);
		ff.strokeJoin(strokeJoin);
		
		Colour colour = colourPalette.current();
		
		pg.stroke(colour.red, colour.green, colour.blue);
		pg.line(px, py, pxx, pyy);
		
		pg.endDraw();
	}
	
	public String getName() {
		return name;
	}
	
}
