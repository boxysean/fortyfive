package dev.boxy.fortyfive;

import java.util.*;

/**
 * Singleton Random class for reproduceable results!
 * @author boxysean
 *
 */
public class RandomSingleton {
	
	private RandomSingleton() {
		
	}
	
	public static RandomSingleton INSTANCE;
	
	public static RandomSingleton getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new RandomSingleton();
		}
		
		return INSTANCE;
	}
	
	protected Random random = new Random();
	
	public Random get() {
		return random;
	}
	
}
