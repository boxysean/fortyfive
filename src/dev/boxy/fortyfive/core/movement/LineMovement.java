package dev.boxy.fortyfive.core.movement;

import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.scene.*;

public abstract class LineMovement {
	
	public static final int		DIR_PREFERRED		= 2;
	public static final int		DIR_AVOID			= 1;
	public static final int		DIR_DISALLOWED		= 0;
	
		// 0 = top, 1 = top right, ..., 7 = top left
	public static final int[]	dr		= new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	public static final int[]	dc		= new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	
	protected Scene scene;
	protected Line line;
	protected String name;
	
	public LineMovement(Scene scene, Line line, String name) {
		this.scene = scene;
		this.line = line;
		this.name = name;
	}
	
	public abstract boolean forwardOnce();
	
	public boolean hasMove(int cr, int cc, int cd) {
		for (int i = -1; i <= 1; i++) {
			int nd = cd + i;
			
			if (nd < 0) {
				nd += 8;
			} else if (nd >= 8) {
				nd -= 8;
			}
			
			int nr = cr + dr[nd];
			int nc = cc + dc[nd];
			
			if (!scene.invalidMove(cr, cc, nr, nc)) {
				return true;
			}
		}
		
		return false;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract int getDirection(int d);
	
}
