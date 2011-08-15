package dev.boxy.fortyfive.core.draw;

import java.util.*;

import processing.core.*;
import dev.boxy.fortyfive.core.colour.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class SolidDrawFactory implements ConfigLoader, LineDrawFactory {
	
	protected Logger logger = Logger.getInstance();
	
	protected String name;
	protected String paletteName;
	protected int strokeWidth;
	protected int strokeJoin;
	protected int strokeCap;
	
	public SolidDrawFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public SolidDraw get(Scene scene) {
		ColourPalette colourPalette = scene.getColourPalette(paletteName);
		
		return new SolidDraw(name, colourPalette, strokeWidth, strokeJoin, strokeCap);
	}

	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		
		if (map.containsKey("palette")) {
			paletteName = (String) map.get("palette");
		}
		
		strokeWidth = ConfigParser.getInt(map, "strokeWidth", 1);
		
		String strokeJoinStr = ConfigParser.getString(map, "strokeJoin", "miter").toLowerCase();
		String strokeCapStr = ConfigParser.getString(map, "strokeCap", "round").toLowerCase();
		
		if (strokeJoinStr.equals("miter")) {
			strokeJoin = PGraphics.MITER; // 8
		} else if (strokeJoinStr.equals("bevel")) {
			strokeJoin = PGraphics.BEVEL; // 32
		} else if (strokeJoinStr.equals("round")) {
			strokeJoin = PGraphics.ROUND; // 2
		} else {
			Logger.getInstance().warning("line draw init: no such stroke join as %s, defaulting to miter\n", strokeJoin);
		}
		
		if (strokeCapStr.equals("round")) {
			strokeCap = PGraphics.ROUND; // 2
		} else if (strokeCapStr.equals("square")) {
			strokeCap = PGraphics.SQUARE; // 1
		} else if (strokeCapStr.equals("project")) {
			strokeCap = PGraphics.PROJECT; // 4
		} else {
			Logger.getInstance().warning("line draw init: no such stroke cap as %s, defaulting to round\n", strokeCap);
		}
	}
	
	public String getName() {
		return name;
	}
	
}
