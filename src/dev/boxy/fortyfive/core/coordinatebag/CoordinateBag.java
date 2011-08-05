package dev.boxy.fortyfive.core.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.core.startarea.*;

public interface CoordinateBag {
	
	public static final CoordinateBag DEFAULT = new RandomBag();
	
	public void initList(List<Coordinate> coords);
	
}
