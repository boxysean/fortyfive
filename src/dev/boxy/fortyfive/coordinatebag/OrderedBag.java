package dev.boxy.fortyfive.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.StartArea.*;

/**
 * Orders the coordinates in linear fashion
 * @author boxysean
 *
 */
public class OrderedBag implements CoordinateBag {
	
	int leftFirst;
	int topFirst;
	
	FortyFive ff;
	
	public OrderedBag(FortyFive ff, int leftFirst, int topFirst) {
		this.ff = ff;
		this.topFirst = topFirst;
		this.leftFirst = leftFirst;
	}
	
	public OrderedBag(FortyFive ff, boolean forward) {
		this(ff, forward ? 2 : -1, forward ? 1 : -2);
	}
	
	public void initList(List<Coordinate> coords, boolean cached) {
		if (cached) {
			return;
		}
		
		if (topFirst == 0 || leftFirst == 0) {
			Collections.shuffle(coords);
		}
		
		Comparator<Coordinate> comparator = new Comparator<Coordinate>() {
			public int compare(Coordinate o1, Coordinate o2) {
				if ((leftFirst >= topFirst || topFirst == 0) && leftFirst != 0) {
					if (o1.c == o2.c && topFirst == 0) {
						// If the columns are the same and topFirst is 0, shuffle the column
						return 2 * ((int) ff.random(2)) - 1;
					} else if (o1.c < o2.c) {
						return (leftFirst > 0) ? -1 : 1;
					} else if (o1.c > o2.c) {
						return (leftFirst > 0) ? 1 : -1;
					} else if (o1.r == o2.r && topFirst == 0) {
						return 2 * ((int) ff.random(2)) - 1;
					} else if (o1.r < o2.r) {
						return (topFirst > 0) ? -1 : 1;
					} else if (o1.r > o2.r) {
						return (topFirst > 0) ? 1 : -1;
					} else {
						return 0;
					}
				} else {
					if (o1.r == o2.r && leftFirst == 0) {
						// If the rows are the same and leftFirst is 0, shuffle the row
						return 2 * ((int) ff.random(2)) - 1;
					} else if (o1.r < o2.r) {
						return (topFirst >= 0) ? -1 : 1;
					} else if (o1.r > o2.r) {
						return (topFirst >= 0) ? 1 : -1;
					} else if (o1.c == o2.c && leftFirst == 0) {
						return 2 * ((int) ff.random(2)) - 1;
					} else if (o1.c < o2.c) {
						return (leftFirst >= 0) ? -1 : 1;
					} else if (o1.c > o2.c) {
						return (leftFirst >= 0) ? 1 : -1;
					} else {
						return 0;
					}
				}
			}
		};
		
		Collections.sort(coords, comparator);
	}
	
	@Override
	public int hashCode() {
		int res = getClass().hashCode();
		res = (res * 31) + leftFirst;
		res = (res * 31) + topFirst;
		return res;
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			OrderedBag ob = (OrderedBag) o;
			return leftFirst == ob.leftFirst && topFirst == ob.topFirst;
		} catch (Exception e) {
			return false;
		}
	}
	
}
