package dev.boxy.fortyfive.draw;

import dev.boxy.fortyfive.*;

public class SolidDraw implements LineDraw {
	
	int red;
	int green;
	int blue;
	
	int strokeWidth;
	
	public SolidDraw(int red, int green, int blue, int strokeWidth) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		
		this.strokeWidth = strokeWidth;
	}
	
	public void drawLine(FortyFive ff, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy) {
		ff.strokeWeight(strokeWidth);
		ff.stroke(red, green, blue);
		ff.line(px, py, pxx, pyy);
	}
	
}
