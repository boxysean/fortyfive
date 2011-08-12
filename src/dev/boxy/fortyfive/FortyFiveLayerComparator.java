package dev.boxy.fortyfive;

import java.util.*;

public class FortyFiveLayerComparator implements Comparator<FortyFiveLayer> {

	public int compare(FortyFiveLayer A, FortyFiveLayer B) {
		int Av = A.getOrder();
		int Bv = B.getOrder();
		
		if (Av < Bv) {
			return -1;
		} else if (Av > Bv) {
			return 1;
		} else {
			return 0;
		}
	}

}
