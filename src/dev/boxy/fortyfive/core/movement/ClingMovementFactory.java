package dev.boxy.fortyfive.core.movement;

import java.util.*;

import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ClingMovementFactory implements LineMovementFactory {
	
	/**
	 * @defgroup ClingMovement
	 * @ingroup movements
	 * 
	 * @{
	 */
	
	/** movement name [required] */
	protected String name;
	
	/** direction string (e.g., "221000012" for N, NE, ..., W, NW) [default: "22222222"] */
	protected int[] direction;
	
	/** @} */
	
	public ClingMovementFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public LineMovement get(Scene scene, Line line) {
		return new ClingMovement(scene, line, name, direction);
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		
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
