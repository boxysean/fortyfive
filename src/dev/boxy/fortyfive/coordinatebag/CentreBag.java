package dev.boxy.fortyfive.coordinatebag;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.StartArea.*;

/**
 * Orders the coordinates in linear fashion
 * @author boxysean
 *
 */
public class CentreBag implements CoordinateBag {
	
	FortyFive ff;
	
	public CentreBag(FortyFive ff) {
		this.ff = ff;
	}
	
	public void initList(List<Coordinate> coords, boolean cached) {
		if (cached) {
			return;
		}
		
		Collections.shuffle(coords);
		
		Comparator<Coordinate> comparator = new Comparator<Coordinate>() {
			public int compare(Coordinate o1, Coordinate o2) {
				int r1 = o1.r;
				int c1 = o1.c;
				int r2 = o2.r;
				int c2 = o2.c;
				
				int halfRows = ff.rows() / 2;
				int halfColumns = ff.columns() / 2;
				
				double score1 = Math.sqrt((halfRows - r1) * (halfRows - r1) + (halfColumns - c1) * (halfColumns - c1));
				double score2 = Math.sqrt((halfRows - r2) * (halfRows - r2) + (halfColumns - c2) * (halfColumns - c2));
				
				if (score1 - 1e-7 > score2) {
					return 1;
				} else if (score2 - 1e-7 > score1) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		
		Collections.sort(coords, comparator);
	}
	
	@Override
	public int hashCode() {
		int res = getClass().hashCode();
		return res;
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			CentreBag cb = (CentreBag) o;
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
}
