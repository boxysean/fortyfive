package dev.boxy.fortyfive.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.StartArea.*;

/**
 * Orders the coordinates in linear fashion
 * @author boxysean
 *
 */
public class OrderedBag implements CoordinateBag {
	
	boolean forward;
	
	public OrderedBag() {
		this(true);
	}
	
	public OrderedBag(boolean forward) {
		this.forward = forward;
	}
	
	public void initList(List<Coordinate> coords) {
		Collections.sort(coords, new Comparator<Coordinate>() {
			
			public int compare(Coordinate o1, Coordinate o2) {
				int comp = o1.compareTo(o2);
				
				comp *= forward ? -1 : 1;
				
				if (comp > 0) {
					return 1;
				} else if (comp < 0) {
					return -1;
				} else { 
					return 0;
				}
			}
			
		});
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode() * 2 + (forward ? 1 : 0);
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			OrderedBag ob = (OrderedBag) o;
			return forward == ob.forward;
		} catch (Exception e) {
			return false;
		}
	}
	
}
