package dev.boxy.fortyfive;

import processing.core.*;
import dev.boxy.fortyfive.draw.*;
import dev.boxy.fortyfive.movement.*;

public class Line {
	
	FortyFive ff;
	
	public int cr;
	public int cc;
	public int cd;
	
	int stepSpeed;
	int drawSpeed;
	
	public double straightProb;
	
	LineDraw draw;
	
	LineMovement movement;
	
	public int[] direction;
	
	LineTemplate template;
	
	public boolean[][] blocked;
	
	public Line(int br, int bc, int bd, FortyFive ff, LineTemplate template) {
		cr = br;
		cc = bc;
		cd = bd;
		
		this.ff = ff;
		this.template = template;
		
		this.stepSpeed = template.stepSpeed;
		this.drawSpeed = template.drawSpeed;
		
		this.straightProb = template.straightProb;
		
		this.movement = template.lineMovement.clone(this);
		
		this.direction = template.direction;
		
		this.draw = template.draw;
		
		// Apply threshold image
		
		if (template.thresholdImage != null) {
			applyThresholdImage(template.thresholdImage);
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
		return ff.invalidMove(cr, cc, nr, nc, blocked);
	}
	
	public boolean forwardDraw() {
		for (int i = 0; i < drawSpeed * ff.drawSpeedMultiplier; i++) {
			int r = cr;
			int c = cc;
			
			if (!forward()) {
				return false;
			}
			
			ff.drawLine(r, c, cr, cc, draw);
		}
		
		return true;
	}
	
	/**
	 * Use image grid's threshold as blocked area for this line
	 * @param grid
	 */
	public void applyThresholdImage(ImageGrid grid) {
		if (blocked == null) {
			blocked = new boolean[ff.rows()][ff.columns()];
		}
		
		grid.applyThreshold(blocked);
	}
	
}