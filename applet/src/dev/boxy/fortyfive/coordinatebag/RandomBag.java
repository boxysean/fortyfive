package dev.boxy.fortyfive.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.StartArea.*;

public class RandomBag implements CoordinateBag {

	public void initList(List<Coordinate> coords) {
		Collections.shuffle(coords);
	}

}
