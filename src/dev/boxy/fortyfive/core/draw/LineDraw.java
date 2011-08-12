package dev.boxy.fortyfive.core.draw;

import processing.core.*;
import dev.boxy.fortyfive.*;

public abstract class LineDraw {
	
	protected String name;
	protected int strokeJoin = FortyFive.MITER;
	protected int strokeCap = FortyFive.ROUND;
	
	public LineDraw(String name, String strokeJoinStr, String strokeCapStr) {
		this.name = name;
		
		if (strokeJoinStr.equals("miter")) {
			strokeJoin = FortyFive.MITER;
		} else if (strokeJoinStr.equals("bevel")) {
			strokeJoin = FortyFive.BEVEL;
		} else if (strokeJoinStr.equals("round")) {
			strokeJoin = FortyFive.ROUND;
		} else {
			System.err.printf("line draw init warning: no such stroke join as %s, defaulting to miter\n", strokeJoin);
		}
		
		if (strokeCapStr.equals("round")) {
			strokeCap = FortyFive.ROUND;
		} else if (strokeCapStr.equals("square")) {
			strokeCap = FortyFive.SQUARE;
		} else if (strokeCapStr.equals("project")) {
			strokeCap = FortyFive.PROJECT;
		} else {
			System.err.printf("line draw init warning: no such stroke cap as %s, defaulting to round\n", strokeCap);
		}
	}
	
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
	public abstract void drawLine(PGraphics pg, int gr, int gc, int grr, int gcc, float px, float py, float pxx, float pyy);
	
	public String getName() {
		return name;
	}
	
}
