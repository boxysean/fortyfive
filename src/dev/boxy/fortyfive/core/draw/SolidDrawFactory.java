package dev.boxy.fortyfive.core.draw;

import java.util.*;

import dev.boxy.fortyfive.core.colour.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class SolidDrawFactory implements ConfigLoader, LineDrawFactory {
	
	protected Logger logger = Logger.getInstance();
	
	/**
	 * @defgroup SolidDraw
	 * @ingroup lineDraws
	 * @{
	 */
	
	/** line draw name [required] **/
	protected String name;
	
	/** colour palette name [required] [alternative: palette] */
	protected String paletteName;
	
	/** stroke width [default: 1] */
	protected int strokeWidth;
	
	/** @} */
	
	public SolidDrawFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		loadSettings(sceneFactory, map);
	}
	
	public SolidDraw get(Scene scene) {
		ColourPalette colourPalette = scene.getColourPalette(paletteName);
		
		return new SolidDraw(name, colourPalette, strokeWidth);
	}

	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		paletteName = ConfigParser.getString(map, new String[] { "palette", "paletteName" });
		strokeWidth = ConfigParser.getInt(map, "strokeWidth", 1);
	}
	
	public String getName() {
		return name;
	}
	
}
