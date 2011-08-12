package dev.boxy.fortyfive.core.line;

import java.util.*;

import dev.boxy.fortyfive.core.draw.*;
import dev.boxy.fortyfive.core.image.*;
import dev.boxy.fortyfive.core.movement.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class LineFactory implements ConfigLoader {
	
	public final static double 		DEFAULT_STRAIGHT_PROB 	= 0.80;
	public final static int 		DEFAULT_STEP_SPEED 		= 1;
	public final static int 		DEFAULT_DRAW_SPEED 		= 1;
	
	protected String				name;
	protected double				straightProb;
	protected int					stepSpeed;
	protected int					drawSpeed;
	protected int[]					direction;
	protected String				lineMovementName;
	protected String				lineDrawName;
	protected String				startAreaName;
	protected List<String>			imageThresholdNames;
	
	protected boolean[][]			blocked;
	
	public LineFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public Line get(Scene scene, int br, int bc, int bd) {
		LineMovementFactory lineMovementFactory = scene.getLineMovementFactory(lineMovementName);
		LineDraw lineDraw = scene.getLineDraw(lineDrawName);
		
		Line line = new Line(scene, br, bc, bd, stepSpeed, drawSpeed, straightProb, direction, lineMovementFactory, lineDraw);
		line.applyBlocked(blocked);
		
		return line;
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		straightProb = ConfigParser.getDouble(map, "straightProb", DEFAULT_STRAIGHT_PROB);
		stepSpeed = ConfigParser.getInt(map, "stepSpeed", DEFAULT_STEP_SPEED);
		drawSpeed = ConfigParser.getInt(map, "drawSpeed", DEFAULT_DRAW_SPEED);
		
		// Parse direction string
		
		String directionStr = ConfigParser.getString(map, "direction", "22222222");
		direction = new int[8];
		
		if (directionStr != null) {
			for (int i = 0; i < Math.min(directionStr.length(), direction.length); i++) {
				direction[i] = (int) directionStr.charAt(i) - '0';
			}
		}
		
		lineMovementName = ConfigParser.getString(map, "movement");
		lineDrawName = ConfigParser.getString(map, new String[] { "draw", "linedraw" });
		startAreaName = ConfigParser.getString(map, "startArea");
		
		imageThresholdNames = ConfigParser.getStrings(map, "threshold");
		
		initThresholds(sceneFactory);
	}
	
	protected void initThresholds(SceneFactory sceneFactory) {
		if (imageThresholdNames != null) {
			blocked = new boolean[sceneFactory.rows()][sceneFactory.columns()];
			
			for (boolean[] b : blocked) {
				Arrays.fill(b, true);
			}
			
			for (String imageThresholdName : imageThresholdNames) {
				ImageThreshold imageThreshold = sceneFactory.getImageThreshold(imageThresholdName);
				imageThreshold.apply(blocked, ImageGrid.MODE_AND);
			}
		}
	}
	
	public int getDirection(int d) {
		return direction[d];
	}
	
	public String getStartAreaName() {
		return startAreaName;
	}
	
	public String getName() {
		return name;
	}

}