package dev.boxy.fortyfive.core.draw;

import processing.core.*;

public interface LineDraw {
	
	/**
	 * 
	 * @param ff
	 * @param gr origin grid row
	 * @param gc origin grid column
	 * @param grr destination grid row
	 * @param gcc destination grid column
	 * @param px origin pixel x-coordinate
	 * @param py origin pixel y-coordinate
	 * @param pxx destination pixel x-coordinate
	 * @param pyy destination pixel y-coordinate
	 */
	public void drawLine(PGraphics pg, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy);
	
	public String getName();
	
}
