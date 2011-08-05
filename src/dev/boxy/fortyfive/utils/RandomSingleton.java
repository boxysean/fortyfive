package dev.boxy.fortyfive.utils;

import java.util.*;

/**
 * Singleton Random class for reproduceable results!
 * @author boxysean
 *
 */
public class RandomSingleton {
	
	private RandomSingleton() {
		
	}
	
	private static RandomSingleton INSTANCE;
	
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
	
	public static int nextInt() {
		return getInstance().random.nextInt();
	}
	
	public static int nextInt(int n) {
		return getInstance().random.nextInt(n);
	}
	
	public static double nextDouble() {
		return getInstance().random.nextDouble();
	}
	
}
