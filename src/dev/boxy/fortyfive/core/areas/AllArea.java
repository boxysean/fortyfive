package dev.boxy.fortyfive.core.areas;

import java.util.*;

public class AllArea implements Area {
	
	public void add(boolean[][] blocked) {
		for (boolean[] b : blocked) {
			Arrays.fill(b, true);
		}
	}
	
	public void subtract(boolean[][] blocked) {
		for (boolean[] b : blocked) {
			Arrays.fill(b, false);
		}
	}
	
	public void set(boolean[][] blocked) {
		add(blocked);
	}
	
	public void unset(boolean[][] blocked) {
		subtract(blocked);
	}
	
}
