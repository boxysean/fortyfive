package dev.boxy.fortyfive.core.draw;

import java.util.*;

import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ImageDrawFactory implements ConfigLoader, LineDrawFactory {

	/**
	 * @defgroup ImageDraw
	 * @ingroup lineDraws
	 * Uses images to choose the colour of lines which correspond to said image
	 * @{
	 */
	
	/** line draw name [required] */
	protected String name;
	
	/** stroke width [default: 1] */
	protected int strokeWidth;

	/** name of the image to draw [required] [alternative: image] */
	protected String imageName;

	/** xOffset placement [default: 0] */
	protected int xOffset;
	
	/** yOffset placement [default: 0] */
	protected int yOffset;
	
	/** image scale [default: 1.0] */
	protected double scale;
	
	/** @} */
	
	public ImageDrawFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public ImageDraw get(Scene scene) {
		return new ImageDraw(scene, name, scene.getImageGrid(imageName), strokeWidth, xOffset, yOffset, scale);
	}

	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		strokeWidth = ConfigParser.getInt(map, "strokeWidth", 1);
		imageName = ConfigParser.getString(map, new String[] { "image", "imageName" });
		xOffset = ConfigParser.getInt(map, "xOffset", 0);
		yOffset = ConfigParser.getInt(map, "yOffset", 0);
		scale = ConfigParser.getDouble(map, "scale", 1.0);
	}
	
	public String getName() {
		return name;
	}
	
}
