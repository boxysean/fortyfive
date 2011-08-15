package dev.boxy.fortyfive.core.colour;

import java.util.*;

import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ColourFactory implements ConfigLoader {
	
	/**
	 * @defgroup colours colours
	 * The colours that may be used in the template
	 * @{
	 */

	/** colour name [required] */
	protected String name;
	
	/** red component [default: 0] */
	protected int red;
	
	/** green component [default: 0] */
	protected int green;
	
	/** blue component [default: 0] */
	protected int blue;
	
	/** @} */
	
	public ColourFactory(SceneFactory sceneFactory, Map<String, Object> masterMap) {
		loadSettings(sceneFactory, masterMap);
	}
	
	public ColourFactory(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public Colour get() {
		return new Colour(name, red, green, blue);
	}
	
	public String getName() {
		return name;
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		red = ConfigParser.getInt(map, "red", 0);
		green = ConfigParser.getInt(map, "green", 0);
		blue = ConfigParser.getInt(map, "blue", 0);
	}
	
}
