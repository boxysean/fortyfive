package dev.boxy.fortyfive.core.image;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ImageThresholdFactory implements ConfigLoader {
	
//	protected SceneFactory sceneFactory;
	
	protected String name;
	protected ImageGrid imageGridName;
	protected boolean invert;
	protected int xOffset;
	protected int yOffset;
	protected double scale;
	
	public ImageThresholdFactory(SceneFactory sceneFactory, Map<String, Object> map) {
//		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public ImageThreshold get() {
		return new ImageThreshold(name, imageGridName, invert, xOffset, yOffset, scale);
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		FortyFive ff = FortyFive.getInstance();
		
		name = (String) map.get("name");
		invert = ConfigParser.getBoolean(map, "invert", false);
		xOffset = ConfigParser.getInt(map, "xOffset", 0);
		yOffset = ConfigParser.getInt(map, "yOffset", 0);
		scale = ConfigParser.getDouble(map, "scale", 1.0);
		
		ImageGrid thresholdImage = sceneFactory.getImageGrid(name);
		
		if (scale < 0) {
			// Auto scale to size of screen
			int imageWidth = thresholdImage.getWidth();
			int imageHeight = thresholdImage.getHeight();
			
			double widthResizeRatio = (double)  ff.getWidth() / imageWidth;
			double heightResizeRatio = (double) ff.getHeight() / imageHeight;
			
			if (scale < -5) {
				// Scale clips off bigger edges
				scale = Math.max(widthResizeRatio, heightResizeRatio);
			} else {
				// Scale does not clip
				scale = Math.min(widthResizeRatio, heightResizeRatio);
			}
		}
		
		if (xOffset < 0) {
			// Center the image along x
			xOffset = (int) (ff.getWidth() - (thresholdImage.getWidth() * scale)) / 2;
		}
		
		if (yOffset < 0) {
			// Center the image along x
			yOffset = (int) (ff.getHeight() - (thresholdImage.getHeight() * scale)) / 2;
		}
	}

}