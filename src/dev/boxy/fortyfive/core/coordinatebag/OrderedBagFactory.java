package dev.boxy.fortyfive.core.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class OrderedBagFactory implements CoordinateBagFactory {

	protected SceneFactory sceneFactory;
	
	/**
	 * @defgroup OrderedBag
	 * @ingroup coordinateBags
	 * Orders coordinates from centre outward
	 * @{
	 */
	
	/** coordinate bag name [required] */
	protected String name;
	
	/** order type (forward, backward, or [leftFirst, topFirst]) [default: forward] */
	protected String order;
	
	/** @} */
	
	protected boolean forward;
	protected boolean useValues;
	protected int leftFirst;
	protected int topFirst;
	
	public OrderedBagFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public CoordinateBag get() {
		if (useValues) {
			return new OrderedBag(leftFirst, topFirst);
		} else {
			return new OrderedBag(forward);
		}
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		
		try {
			// Maybe the coord bag is a list of integers
			
			List<Integer> coordBagValues = (List<Integer>) map.get("order");
			
			leftFirst = coordBagValues.get(0);
			topFirst = coordBagValues.get(1);
			useValues = true;
			
			return;
		} catch (Exception e) {
			
		}
		
		order = ConfigParser.getString(map, "order", "forward");
		
		if (order.equalsIgnoreCase("backward")) {
			forward = false;
		} else {
			forward = true;
		}
	}
	
	public String getName() {
		return name;
	}
	
}
