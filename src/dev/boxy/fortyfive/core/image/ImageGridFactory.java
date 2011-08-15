package dev.boxy.fortyfive.core.image;

import java.io.*;
import java.util.*;

import processing.core.*;
import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ImageGridFactory implements ConfigLoader {
	
	protected SceneFactory sceneFactory;
	
	/**
	 * @defgroup images images
	 * The images that may be used in the template
	 * @{
	 */

	/** image name [required] */
	protected String name;
	
	/** image file [required] */
	protected String file;
	
	/** @} */
	
	protected PImage image;
	
	public ImageGridFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public ImageGrid get() {
		return new ImageGrid(sceneFactory, name, file, image);
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		file = ConfigParser.getString(map, new String[] { "file", "image" });
		
		if (!new File(file).exists()) {
			Logger.getInstance().warning("image load: could not file for image %s", name);
		}

		FortyFive ff = FortyFive.getInstance();

		image = ff.loadImage(file);
		
		if (image == null) {
			Logger.getInstance().warning("image load: image %s failed to load", name);
		}
	}

}