package dev.boxy.fortyfive.core.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.core.startarea.*;

public class RandomBag implements CoordinateBag {

	public void initList(List<Coordinate> coords) {
		Collections.shuffle(coords);
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			RandomBag ob = (RandomBag) o;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
