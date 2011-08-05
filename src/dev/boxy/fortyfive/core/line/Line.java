package dev.boxy.fortyfive.core.line;

import dev.boxy.fortyfive.core.draw.*;
import dev.boxy.fortyfive.core.image.*;
import dev.boxy.fortyfive.core.movement.*;
import dev.boxy.fortyfive.core.scene.*;

public class Line {
	
	protected Scene scene;
	
	public int cr;
	public int cc;
	public int cd;
	
	protected int stepSpeed;
	protected int drawSpeed;
	
	public double straightProb;
	
	protected LineDraw draw;
	
	protected LineMovement movement;
	
	public int[] direction;
	
	public boolean[][] blocked;
	
	public Line(Scene scene, int br, int bc, int bd, int stepSpeed, int drawSpeed, double straightProb, int[] direction, LineMovementFactory lineMovementFactory, LineDraw lineDraw) {
		this.scene = scene;
		
		cr = br;
		cc = bc;
		cd = bd;
		
		this.stepSpeed = stepSpeed;
		this.drawSpeed = drawSpeed;
		this.straightProb = straightProb;
		this.direction = direction;
		this.movement = lineMovementFactory.get(scene, this);
		this.draw = lineDraw;
		this.blocked = new boolean[scene.rows()][scene.columns()];
		
		// Apply threshold blocking
		
		for (ImageThreshold threshold : scene.getImageThresholds()) {
			if (blocked == null) {
				blocked = new boolean[scene.rows()][scene.columns()];
			}

			threshold.apply(blocked);
		}
	}
	
	public boolean forward() {
		for (int i = 0; i < stepSpeed; i++) {
			if (!forwardOnce()) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean forwardOnce() {
		return movement.forwardOnce();
	}
	
	public boolean invalidMove(int nr, int nc) {
		return scene.invalidMove(cr, cc, nr, nc, blocked);
	}
	
	public boolean forwardDraw() {
		int r = cr;
		int c = cc;
		
		if (!forward()) {
			return false;
		}
		
		scene.drawLine(r, c, cr, cc, draw);
		
		return true;
	}
	
	public int getSpeed() {
		return drawSpeed;
	}
	
}