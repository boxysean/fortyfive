package dev.boxy.fortyfive.core.movement;

import java.util.*;

import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class IntelligentMovementFactory implements LineMovementFactory {

	/**
	 * @defgroup IntelligentMovement
	 * @ingroup movements
	 * 
	 * @{
	 */
	
	/** movement name [required] */
	protected String name;
	
	/** intelligence of the line (0-3, higher is smarter) [default: 2] */
	protected int intelligence;
	
	/** probability that the line will continue straight [default: 0.8] */
	protected double straightProb;
	
	/** direction string (e.g., "221000012" for N, NE, ..., W, NW) [default: "22222222"] */
	protected int[] direction;
	
	public IntelligentMovementFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public LineMovement get(Scene scene, Line line) {
		return new IntelligentMovement(scene, line, name, straightProb, direction, intelligence);
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		straightProb = ConfigParser.getDouble(map, "straightProb", 0.8);
		intelligence = ConfigParser.getInt(map, "intelligence", 2);
		
		// Parse direction string
		
		String directionStr = ConfigParser.getString(map, "direction", "22222222");
		direction = new int[8];
		
		if (directionStr != null) {
			for (int i = 0; i < Math.min(directionStr.length(), direction.length); i++) {
				direction[i] = (int) directionStr.charAt(i) - '0';
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public int getDirection(int d) {
		return direction[d];
	}
	
}
