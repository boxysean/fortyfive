package dev.boxy.fortyfive.core.startarea;

public class Coordinate implements Comparable<Coordinate> {
	public int r;
	public int c;
	
	public Coordinate(int r, int c) {
		this.r = r;
		this.c = c;
	}

	public int compareTo(Coordinate o) {
		if (o.r < r) {
			return -1;
		} else if (o.r > r) {
			return 1;
		} else if (o.c < c) {
			return -1;
		} else if (o.c > c) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public boolean equals(Object o) {
		try {
			Coordinate coord = (Coordinate) o;
			return coord.r == r && coord.c == c;
		} catch (Exception e) {
			return false;
		}
	}
	
	public int hashCode() {
		return (r << 8) + c; 
	}
}