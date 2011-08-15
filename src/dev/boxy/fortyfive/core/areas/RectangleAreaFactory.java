package dev.boxy.fortyfive.core.areas;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class RectangleAreaFactory implements ConfigLoader {
	
	protected SceneFactory sceneFactory;
	
	/**
	 * @defgroup RectangleArea RectangleArea
	 * @ingroup areas
	 * Use "type: rectangle"
	 * 
	 * @{
	 */

	/** rectangle area name [required] */
	protected String name;
	
	/** rectangle x [default: 0] */
	protected int x;
	
	/** rectangle y [default: 0] */
	protected int y;
	
	/** rectangle width [default: screen width] */
	protected int width;
	
	/** rectangle height [default: screen height] */
	protected int height;
	
	/** @} */
	
	public RectangleAreaFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public RectangleArea get() {
		return new RectangleArea(sceneFactory, name, x, y, width, height);
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		
		FortyFive ff = FortyFive.getInstance();
		int ffwidth = ff.getWidth();
		int ffheight = ff.getHeight();
		
		x = ConfigParser.parseInt(map, "x", ffwidth, 0);
		y = ConfigParser.parseInt(map, "y", ffheight, 0);
		width = ConfigParser.parseInt(map, "width", ffwidth, ffwidth);
		height = ConfigParser.parseInt(map, "height", ffheight, ffheight);
	}

}