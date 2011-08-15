package dev.boxy.fortyfive.core.areas;

/**
 * @defgroup areas areas
 */

public interface Area {
	
	public void add(boolean[][] blocked);
	public void subtract(boolean[][] blocked);
	public void set(boolean[][] blocked);
	public void unset(boolean[][] blocked);
	
}
