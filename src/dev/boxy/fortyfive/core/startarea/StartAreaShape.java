package dev.boxy.fortyfive.core.startarea;

public abstract class StartAreaShape implements Comparable<StartAreaShape> {
	static int MASTER_ID = 0;
	
	int id;
	
	public StartAreaShape() {
		this.id = MASTER_ID++;
	}
	
	public int compareTo(StartAreaShape s) {
		return id - s.id;
	}
	
	abstract void apply(boolean[][] blocked);
}