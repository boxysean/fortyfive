package dev.boxy.fortyfive.draw;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.colour.*;

public class SolidDrawFactory implements FortyFiveLoader, LineDrawFactory {
	
	public static final SolidDrawFactory DEFAULT = new SolidDrawFactory(ColourPaletteFactory.DEFAULT, 1, "miter", "round");
	
	protected Logger logger = Logger.getInstance();
	
	protected ColourPaletteFactory paletteFactory;
	protected int strokeWidth;
	protected String strokeJoinStr;
	protected String strokeCapStr;
	
	public SolidDrawFactory(Map<String, Object> map) {
		loadSettings(map);
	}
	
	public SolidDrawFactory(ColourPaletteFactory paletteFactory, int strokeWidth, String strokeJoinStr, String strokeCapStr) {
		this.paletteFactory = paletteFactory;
		this.strokeWidth = strokeWidth;
		this.strokeJoinStr = strokeJoinStr;
		this.strokeCapStr = strokeCapStr;
	}
	
	public void loadSettings(Map<String, Object> map) {
		if (map.containsKey("palette")) {
			ColourPaletteFactoryMap colourPaletteFactories = ColourPaletteFactoryMap.getInstance();
			String paletteName = (String) map.get("palette");
			paletteFactory = colourPaletteFactories.get(paletteName);
			
			if (paletteFactory == null) {
				logger.warning("draw init: no such palette %s, skipping", paletteName);
			}
		} else if (map.containsKey("red") || map.containsKey("green") || map.containsKey("blue")) {
			int red = ConfigParser.getInt(map, "red", 0);
			int green = ConfigParser.getInt(map, "green", 0);
			int blue = ConfigParser.getInt(map, "blue", 0);
			ColourFactory colourFactory = new ColourFactory(red, green, blue);
			paletteFactory = new ColourPaletteFactory(colourFactory);
		}
		
		if (paletteFactory == null) {
			logger.warning("draw init: no palette defined");
			paletteFactory = ColourPaletteFactory.DEFAULT;
		}
		
		strokeWidth = ConfigParser.getInt(map, "strokeWidth", 1);
		strokeJoinStr = ConfigParser.getString(map, "strokeJoin", "miter").toLowerCase();
		strokeCapStr = ConfigParser.getString(map, "strokeCap", "round").toLowerCase();
	}
	
	public SolidDraw get() {
		return new SolidDraw(paletteFactory, strokeWidth, strokeJoinStr, strokeCapStr);
	}

}
