package dev.boxy.fortyfive.core.draw;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ImageDrawFactory implements ConfigLoader, LineDrawFactory {

	protected String name;
	protected int strokeWidth;
	protected String strokeJoinStr;
	protected String strokeCapStr;
	protected String imageGridName;
	protected int xOffset;
	protected int yOffset;
	protected double scale;
	
	public ImageDrawFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public ImageDraw get(Scene scene) {
		return new ImageDraw(scene, name, scene.getImageGrid(imageGridName), strokeWidth, xOffset, yOffset, scale, strokeJoinStr, strokeCapStr);
	}

	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		
		strokeWidth = ConfigParser.getInt(map, "strokeWidth", 0);
		strokeJoinStr = ConfigParser.getString(map, "strokeJoin", "miter").toLowerCase();
		strokeCapStr = ConfigParser.getString(map, "strokeCap", "round").toLowerCase();
		
		imageGridName = ConfigParser.getString(map, "image");
		xOffset = ConfigParser.getInt(map, "xOffset", 0);
		yOffset = ConfigParser.getInt(map, "yOffset", 0);
		scale = ConfigParser.getDouble(map, "scale", 1.0);
	}
	
	public String getName() {
		return name;
	}
	
}
