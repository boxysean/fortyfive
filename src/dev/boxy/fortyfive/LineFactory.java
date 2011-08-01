package dev.boxy.fortyfive;

import java.util.*;

import dev.boxy.fortyfive.draw.*;
import dev.boxy.fortyfive.movement.*;

public class LineFactory implements FortyFiveLoader {
	
	public final static double 			DEF_STRAIGHT_PROB 	= 0.80;
	public final static int 			DEF_STEP_SPEED 		= 1;
	public final static int 			DEF_DRAW_SPEED 		= 1;
	public final static int[]			DEF_DIRECTION		= new int[] { 2, 2, 2, 2, 2, 2, 2, 2 };
//	public final static LineDraw		DEF_DRAW			= new SolidDraw(BaseColourPalette.getDefault(), 1, "miter", "round");
	
	double					straightProb 		= DEF_STRAIGHT_PROB;
	int						stepSpeed			= DEF_STEP_SPEED;
	int						drawSpeed			= DEF_DRAW_SPEED;
	LineMovement			lineMovement		= null;
	int[]					direction 			= new int[8];
	LineDrawFactory			draw				= null;
	StartArea				startArea			= null;
	List<ImageThreshold>	thresholds			= null;
	
	public LineFactory(double straightProb, int stepSpeed, int drawSpeed, LineMovement lineMovement, int[] direction, LineDrawFactory draw, StartArea startArea, List<ImageThreshold> thresholds) {
		this.straightProb = straightProb;
		this.stepSpeed = stepSpeed;
		this.drawSpeed = drawSpeed;
		this.lineMovement = lineMovement;
		this.direction = direction;
		this.draw = draw;
		this.startArea = startArea;
		this.thresholds = thresholds;
	}
	
	public Line newLine(int br, int bc, int bd, FortyFive ff) {
		return new Line(br, bc, bd, ff, this);
	}
	
	public void loadSettings(Map<String, Object> map) {
		
	}

}