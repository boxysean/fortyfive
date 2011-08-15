package dev.boxy.fortyfive.core.areas;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.image.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ImageAreaFactory implements ConfigLoader {
	
	protected SceneFactory sceneFactory;
	
	/**
	 * @defgroup ImageArea ImageArea
	 * @ingroup areas
	 * Use "type: image"
	 * 
	 * @{
	 */

	/** image area name [required] */
	protected String name;
	
	/** image name to be used in this area [required] */
	protected String imageName;
	
	/** xOffset of image [default: 0] */
	protected int xOffset;
	
	/** yOffset of image [default: 0] */
	protected int yOffset;
	
	/** scale of image [default: 1.0] */
	protected double scale;
	
	/** @} */
	
	public ImageAreaFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public ImageArea get() {
		return new ImageArea(sceneFactory, name, imageName, xOffset, yOffset, scale);
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		FortyFive ff = FortyFive.getInstance();
		
		name = ConfigParser.getString(map, "name");
		imageName = ConfigParser.getString(map, new String[] { "image", "imageName" });
		xOffset = ConfigParser.getInt(map, "xOffset", 0);
		yOffset = ConfigParser.getInt(map, "yOffset", 0);
		scale = ConfigParser.getDouble(map, "scale", 1.0);
		
		ImageGrid thresholdImage = sceneFactory.getImageGrid(imageName);
		
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