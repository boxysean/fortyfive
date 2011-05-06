package dev.boxy.fortyfive.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.StartArea.*;

public class RandomBag implements CoordinateBag {

	public void initList(List<Coordinate> coords, boolean cached) {
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
