package dev.boxy.fortyfive.core.draw;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.colour.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class SolidDrawFactory implements ConfigLoader, LineDrawFactory {
	
	protected Logger logger = Logger.getInstance();
	
	protected String name;
	protected String paletteName;
	protected int strokeWidth;
	protected String strokeJoinStr;
	protected String strokeCapStr;
	
	public SolidDrawFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public SolidDrawFactory(String paletteName, int strokeWidth, String strokeJoinStr, String strokeCapStr) {
		this.paletteName = paletteName;
		this.strokeWidth = strokeWidth;
		this.strokeJoinStr = strokeJoinStr;
		this.strokeCapStr = strokeCapStr;
	}
	
	public SolidDraw get(Scene scene) {
		ColourPalette colourPalette = scene.getColourPalette(paletteName);
		
		return new SolidDraw(name, colourPalette, strokeWidth, strokeJoinStr, strokeCapStr);
	}

	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		
		if (map.containsKey("palette")) {
			paletteName = (String) map.get("palette");
		}
		
		strokeWidth = ConfigParser.getInt(map, "strokeWidth", 1);
		strokeJoinStr = ConfigParser.getString(map, "strokeJoin", "miter").toLowerCase();
		strokeCapStr = ConfigParser.getString(map, "strokeCap", "round").toLowerCase();
	}
	
	public String getName() {
		return name;
	}
	
}
