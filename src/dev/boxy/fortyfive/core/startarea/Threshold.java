package dev.boxy.fortyfive.core.startarea;

import dev.boxy.fortyfive.core.image.*;

class Threshold extends StartAreaShape {
	boolean add;
	
	ImageThreshold threshold;
	
	public Threshold(ImageThreshold threshold, boolean add) {
		super();
		
		this.threshold = threshold;
		this.add = add;
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			Threshold t = (Threshold) o;
			
			if (!t.threshold.equals(threshold)) {
				return false;
			} else if (t.add != add) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int res = threshold.getName().hashCode();
		res = res * 2 + (add ? 0 : 1);
		return res;
	}

	@Override
	public void apply(boolean[][] blocked) {
		threshold.apply(blocked);
	}
}