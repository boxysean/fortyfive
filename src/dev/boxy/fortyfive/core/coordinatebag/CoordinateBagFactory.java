package dev.boxy.fortyfive.core.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class CoordinateBagFactory implements ConfigLoader {

	protected SceneFactory sceneFactory;
	
	protected String name;
	protected String type;
	protected boolean useValues;
	protected int leftFirst;
	protected int topFirst;
	
	public CoordinateBagFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public CoordinateBag get() {
		if (useValues) {
			return new OrderedBag(leftFirst, topFirst);
		} else if (type.equalsIgnoreCase("ordered") || type.equalsIgnoreCase("forward")) {
			return new OrderedBag(true);
		} else if (type.equalsIgnoreCase("backward")) {
			return new OrderedBag(false);
		} else if (type.equalsIgnoreCase("random")) {
			return new RandomBag();
		} else if (type.equalsIgnoreCase("centre")) {
			return new CentreBag(sceneFactory);
		} else {
			return null;
		}
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		
		try {
			// Maybe the coord bag is a list of integers
			
			List<Integer> coordBagValues = (List<Integer>) map.get("coordBag");
			
			leftFirst = coordBagValues.get(0);
			topFirst = coordBagValues.get(1);
			type = "ordered";
			useValues = true;
			
			return;
		} catch (Exception e) {
			
		}
		
		type = ConfigParser.getString(map, new String[] { "coordBag", "type", "mode" }, "random");
	}
	
	public String getName() {
		return name;
	}
	
}
