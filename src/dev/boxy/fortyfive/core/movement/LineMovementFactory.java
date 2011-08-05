package dev.boxy.fortyfive.core.movement;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class LineMovementFactory implements ConfigLoader {

	public static final int TYPE_CLING = 0;
	public static final int TYPE_INTELLIGENT = 1;
	
	protected String name;
	protected String type;
	protected int intelligence;
	
	public LineMovementFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public LineMovement get(Scene scene, Line line) {
		if (type.startsWith("cling")) {
			return new ClingMovement(scene, line, name);
		} else if (type.startsWith("intelligent")) {
			return new IntelligentMovement(scene, line, name, intelligence);
		} else {
			return null;
		}
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		type = ConfigParser.getString(map, new String[] { "type", "movement" }, "intelligent").toLowerCase();
		intelligence = ConfigParser.getInt(map, "intelligence", 2);
	}
	
	public String getName() {
		return name;
	}
	
}
