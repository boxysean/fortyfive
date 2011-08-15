package dev.boxy.fortyfive.core.movement;

import java.util.*;

import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class LineMovementFactory implements ConfigLoader {

	protected String name;
	protected String type;
	protected int intelligence;
	protected double straightProb;
	protected int[] direction;
	
	public LineMovementFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public LineMovement get(Scene scene, Line line) {
		if (type.startsWith("cling")) {
			return new ClingMovement(scene, line, name, direction);
		} else if (type.startsWith("intelligent")) {
			return new IntelligentMovement(scene, line, name, straightProb, direction, intelligence);
		} else {
			return null;
		}
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		type = ConfigParser.getString(map, new String[] { "type", "movement" }, "intelligent").toLowerCase();
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
