package dev.boxy.fortyfive.core.colour;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class ColourFactory implements ConfigLoader {
	
	protected static int COLOUR_FACTORY_ID = 0;
	
	public static ColourFactory DEFAULT = new ColourFactory(0, 0, 0);
	
	protected String name;
	protected int red;
	protected int green;
	protected int blue;
	
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
		name = ConfigParser.getString(map, "name", getDefaultName());
		red = ConfigParser.getInt(map, "red", 0);
		green = ConfigParser.getInt(map, "green", 0);
		blue = ConfigParser.getInt(map, "blue", 0);
	}
	
	protected static String getDefaultName() {
		return String.format("default_%05d", COLOUR_FACTORY_ID++);
	}

}
