package dev.boxy.fortyfive.core.draw;

import java.util.*;

import processing.core.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ImageDrawFactory implements ConfigLoader, LineDrawFactory {

	protected String name;
	protected int strokeWidth;
	protected int strokeJoin;
	protected int strokeCap;
	protected String imageGridName;
	protected int xOffset;
	protected int yOffset;
	protected double scale;
	
	public ImageDrawFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public ImageDraw get(Scene scene) {
		return new ImageDraw(scene, name, scene.getImageGrid(imageGridName), strokeWidth, xOffset, yOffset, scale, strokeJoin, strokeCap);
	}

	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		
		strokeWidth = ConfigParser.getInt(map, "strokeWidth", 0);
		
		String strokeJoinStr = ConfigParser.getString(map, "strokeJoin", "miter").toLowerCase();
		String strokeCapStr = ConfigParser.getString(map, "strokeCap", "round").toLowerCase();
		
		imageGridName = ConfigParser.getString(map, "image");
		xOffset = ConfigParser.getInt(map, "xOffset", 0);
		yOffset = ConfigParser.getInt(map, "yOffset", 0);
		scale = ConfigParser.getDouble(map, "scale", 1.0);
		
		if (strokeJoinStr.equals("miter")) {
			strokeJoin = PGraphics.MITER;
		} else if (strokeJoinStr.equals("bevel")) {
			strokeJoin = PGraphics.BEVEL;
		} else if (strokeJoinStr.equals("round")) {
			strokeJoin = PGraphics.ROUND;
		} else {
			Logger.getInstance().warning("line draw init: no such stroke join as %s, defaulting to miter\n", strokeJoin);
		}
		
		if (strokeCapStr.equals("round")) {
			strokeCap = PGraphics.ROUND;
		} else if (strokeCapStr.equals("square")) {
			strokeCap = PGraphics.SQUARE;
		} else if (strokeCapStr.equals("project")) {
			strokeCap = PGraphics.PROJECT;
		} else {
			Logger.getInstance().warning("line draw init: no such stroke cap as %s, defaulting to round\n", strokeCap);
		}
	}
	
	public String getName() {
		return name;
	}
	
}
