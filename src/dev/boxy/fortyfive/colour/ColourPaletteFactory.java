package dev.boxy.fortyfive.colour;

import java.util.*;

import dev.boxy.fortyfive.*;

public class ColourPaletteFactory implements FortyFiveFactory {
	
	protected static int COLOUR_PALETTE_FACTORY_ID = 0;
	
	public static ColourPaletteFactory DEFAULT = new ColourPaletteFactory(ColourFactory.DEFAULT);
	
	protected static final String DEFAULT_ORDER = "linear";

	protected String name;
	protected String order;
	protected List<ColourFactory> colourFactories = new ArrayList<ColourFactory>();
	
	public ColourPaletteFactory(Map<String, Object> map) {
		loadSettings(map);
	}
	
	public ColourPaletteFactory(ColourFactory... colourFactories) {
		for (ColourFactory cf : colourFactories) {
			this.colourFactories.add(cf);
		}
		
		name = getDefaultName();
		order = DEFAULT_ORDER;
	}
	
	public void loadSettings(Map<String, Object> map) {
		name = ConfigParser.getString(map, "name", getDefaultName());
		order = ConfigParser.getString(map, new String[] { "order", "mode" }, "random").toLowerCase();
		
		List<String> colourNames = (List<String>) map.get("colours");
		
		ColourFactoryMap colourFactoryMap = ColourFactoryMap.getInstance();
		Logger logger = Logger.getInstance();
		
		for (String colourName : colourNames) {
			ColourFactory colourFactory = colourFactoryMap.get(colourName);
			
			if (colourFactory == null) {
				logger.warning("ColourPalette init: colour name %s (palette %s) does not exist, skipping", colourName, name); 
			} else {
				colourFactories.add(colourFactory);
			}
		}
		
		logger.debug("new colour palette %s", name);
		
		if (colourFactories.size() == 0) {
			logger.warning("ColourPalette init: colour palette %s is empty", name); 
		}
	}
	
	public ColourPalette get() {
		ColourPalette palette = null;
		
		if (order.startsWith("random")) {
			palette = new RandomColourPalette(name, colourFactories);
		} else if (order.startsWith("linear")) {
			palette = new LinearColourPalette(name, colourFactories);
		}
		
		return palette;
	}
	
	public String getName() {
		return name;
	}

	protected static String getDefaultName() {
		return String.format("default_%05d", COLOUR_PALETTE_FACTORY_ID++);
	}
	
}
