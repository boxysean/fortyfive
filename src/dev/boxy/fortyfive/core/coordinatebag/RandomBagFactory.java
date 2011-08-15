package dev.boxy.fortyfive.core.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class RandomBagFactory implements CoordinateBagFactory {

	protected SceneFactory sceneFactory;
	
	/**
	 * @defgroup RandomBag
	 * @ingroup coordinateBags
	 * Orders coordinates randomly
	 * @{
	 */
	
	/** coordinate bag name [required] */
	protected String name;
	
	/** @} */
	
	public RandomBagFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public CoordinateBag get() {
		return new RandomBag();
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
	}
	
	public String getName() {
		return name;
	}
	
}
