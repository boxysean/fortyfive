package dev.boxy.fortyfive.core.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class CentreBagFactory implements CoordinateBagFactory {

	protected SceneFactory sceneFactory;
	
	/**
	 * @defgroup CentreBag
	 * @ingroup coordinateBags
	 * Orders coordinates from centre outward
	 * @{
	 */
	
	/** coordinate bag name [required] */
	protected String name;
	
	/** @} */
	
	public CentreBagFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public CoordinateBag get() {
		return new CentreBag(sceneFactory);
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
	}
	
	public String getName() {
		return name;
	}
	
}
