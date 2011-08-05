package dev.boxy.fortyfive.core.image;

import java.io.*;
import java.util.*;

import processing.core.*;
import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ImageGridFactory implements ConfigLoader {
	
	protected SceneFactory sceneFactory;
	
	protected String name;
	protected String imageFile;
	protected PImage image;
	
	public ImageGridFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public ImageGrid get() {
		return new ImageGrid(sceneFactory, name, imageFile, image);
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		FortyFive ff = FortyFive.getInstance();
		
		name = (String) map.get("name");
		imageFile = (String) map.get("file");
		
		if (!new File(imageFile).exists()) {
			Logger.getInstance().warning("image load: could not file for image %s", name);
		}

		image = ff.loadImage(imageFile);
		
		if (image == null) {
			Logger.getInstance().warning("image load: image %s failed to load", name);
		}
	}

}