package dev.boxy.fortyfive.core.colour;

import java.util.*;

import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ColourPaletteFactory implements ConfigLoader {
	
	/**
	 * @defgroup colourPalettes colourPalettes
	 * Arrays of colours that are picked in a particular ordering
	 * @{
	 */

	/** colour palette name [required] */
	protected String name;
	
	/** the order in which the colours are produced (random, linear) [default: random] */
	protected String order;
	
	/** names of the colours in this palette [required] */
	protected List<String> colourNames = new ArrayList<String>();
	
	/** @} */
	
	public ColourPaletteFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
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
		name = ConfigParser.getString(map, "name");
		order = ConfigParser.getString(map, new String[] { "order", "mode", "type" }, "random").toLowerCase();
		colourNames = ConfigParser.getStrings(map, new String[] { "colours", "colourNames" });
		
		if (colourNames.size() == 0) {
			Logger.getInstance().warning("ColourPalette init: colour palette %s is empty", name); 
		}
	}
	
	public String getName() {
		return name;
	}
	
}
