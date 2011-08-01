package dev.boxy.fortyfive;

import java.util.*;

public class ImageGridMap {

	private static ImageGridMap INSTANCE;
	
	private ImageGridMap() {
		
	}
	
	public static ImageGridMap getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ImageGridMap();
		}
		
		return INSTANCE;
	}
	
	protected Map<String, ImageGrid> map = new HashMap<String, ImageGrid>();
	
	public void add(ImageGrid imageGrid) {
		map.put(imageGrid.getName(), imageGrid);
	}
	
	public ImageGrid get(String name) {
		return map.get(name);
	}
	
}
