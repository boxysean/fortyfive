package dev.boxy.fortyfive;

import java.util.*;

public abstract class StartArea {
	
	protected FortyFive ff;
	
	// Lists of remaining places available.
	
	LinkedList<Integer>		rrem	= new LinkedList<Integer>();
	LinkedList<Integer>		crem	= new LinkedList<Integer>();
	
	int gr;
	int gc;
	
	public StartArea(FortyFive ff) {
		this.ff = ff;
	}
	
	/**
	 * Finds the next start point
	 * @return true if there is a point at which lines can start, false otherwise 
	 */
	public boolean getNextStartPoint() {
		do {
			if (crem.size() == 0) {
				gr = -1;
				gc = -1;
				return false;
			}
			
			int idx = (int) ff.random(crem.size()-1 - 1e-7f);
			gr = rrem.remove(idx);
			gc = crem.remove(idx);
		} while (ff.grid[gr][gc]);
		
		return true;
	}
	
	public int getStartRow() {
		return gr;
	}
	
	public int getStartColumn() {
		return gc;
	}
	
}
