package dev.boxy.fortyfive.core.startarea;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;

public class Rectangle extends StartAreaShape {
	
	protected SceneFactory sceneFactory;
	
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected boolean block;
	
	public Rectangle(SceneFactory sceneFactory, int x, int y, int width, int height, boolean block) {
		super();
		
		this.sceneFactory = sceneFactory;
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.block = block;
	}
	
	@Override
	public boolean equals(Object o) {
		try {
			Rectangle r = (Rectangle) o;
			
			if (r.x != x) {
				return false;
			} else if (r.y != y) {
				return false;
			} else if (r.width != width) {
				return false;
			} else if (r.height != height) {
				return false;
			} else if (r.block != block) {
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
		int res = x;
		res = (res * 31) + y;
		res = (res * 31) + width;
		res = (res * 31) + height;
		res = (res * 2) + (block ? 0 : 1);
		return res;
	}

	@Override
	public void apply(boolean[][] blocked) {
		FortyFive ff = FortyFive.getInstance();
		
		int gr = sceneFactory.pixelToGrid(y, ff.getHeight(), sceneFactory.rows());
		int gc = sceneFactory.pixelToGrid(x, ff.getWidth(), sceneFactory.columns());
		int grr = sceneFactory.pixelToGrid(y + height, ff.getHeight(), sceneFactory.rows());
		int gcc = sceneFactory.pixelToGrid(x + width, ff.getWidth(), sceneFactory.columns());
		
		int sr = Math.max(gr, 0);
		int sc = Math.max(gc, 0);
		int er = Math.min(grr, sceneFactory.rows());
		int ec = Math.min(gcc, sceneFactory.columns());
		
		for (int r = sr; r < er; r++) {
			for (int c = sc; c < ec; c++) {
				if (block) {
					blocked[r][c] = true;
				} else {
					blocked[r][c] = false;
				}
			}
		}
	}
}