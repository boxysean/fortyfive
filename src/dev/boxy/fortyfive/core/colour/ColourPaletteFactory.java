package dev.boxy.fortyfive.core.colour;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ColourPaletteFactory implements ConfigLoader {
	
	public static final String DEFAULT_ORDER = "linear";

	protected String name;
	protected String order;
	protected List<String> colourNames = new ArrayList<String>();
	
	public ColourPaletteFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public ColourPaletteFactory(ColourFactory colourFactory) {
		this.name = colourFactory.getName();
		this.order = DEFAULT_ORDER;
		colourNames.add(colourFactory.getName());
	}
	
	public ColourPalette get(Scene scene) {
		ColourPalette palette = null;
		
		List<Colour> colours = new ArrayList<Colour>();
		
		for (String colourName : colourNames) {
			colours.add(scene.getColour(colourName));
		}
		
		if (order.startsWith("random")) {
			palette = new RandomColourPalette(name, colours);
		} else if (order.startsWith("linear")) {
			palette = new LinearColourPalette(name, colours);
		}
		
		return palette;
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name"/*, getDefaultName()*/);
		order = ConfigParser.getString(map, new String[] { "order", "mode" }, "random").toLowerCase();
		colourNames = (List<String>) map.get("colours");
		
		if (colourNames.size() == 0) {
			Logger.getInstance().warning("ColourPalette init: colour palette %s is empty", name); 
		}
	}
	
	public String getName() {
		return name;
	}
	
}
