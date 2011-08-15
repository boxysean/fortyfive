package dev.boxy.fortyfive.core.movement;
import java.util.*;

import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;


public class ClingMovement extends LineMovement {
	
	protected int[] direction;
	
	public List<Integer> dlist = new ArrayList<Integer>();
	
	public ClingMovement(Scene scene, Line line, String name, int[] direction) {
		super(scene, line, name);
		
		this.direction = direction;
		
		dlist.add(-1);
		dlist.add(0);
		dlist.add(1);
	}
	
	@Override
	public boolean forwardOnce() {
		Collections.shuffle(dlist);

		int highScore = -1;
		int highScoreDir = -1;
		
		for (int d : dlist) {
			// Pick from the dlist so that we get a random direction chosen first. Therefore we don't keep picking
			// direction -1 over direction +1. 
			int nd = line.cd + d;
			
			if (nd < 0) {
				nd += 8;
			} else if (nd >= 8) {
				nd -= 8;
			}
			
			int nr = line.cr + dr[nd];
			int nc = line.cc + dc[nd];
			
			if (!hasMove(nr, nc, nd)) {
				continue;
			}

			if (scene.invalidMove(line.cr, line.cc, nr, nc, line.blocked)) {
				continue;
			}
			
			// Keep score of how many boxes are filled or are outside the graphique
			
			int score = 0;
			
			for (int j = 0; j < 8; j++) {
				int mr = nr + dr[j];
				int mc = nc + dc[j];
				
				if (mr < 0 || mr >= scene.rows() || mc < 0 || mc >= scene.columns() || scene.checkGrid(mr, mc)) {
					score++;
				}
			}
			
			boolean newHigh = false;
			
			switch (direction[nd]) {
			case LineMovement.DIR_AVOID:
				newHigh = highScoreDir < 0;
				break;
				
			case LineMovement.DIR_PREFERRED:
				newHigh = score > highScore;
				break;
			}
			
			if (newHigh) {
				highScore = score;
				highScoreDir = nd;
			}
		}
		
		if (highScoreDir < 0) {
			TimingUtils.markAdd("cling forwardOnce()");
			return false;
		}
		
		int nr = line.cr + dr[highScoreDir];
		int nc = line.cc + dc[highScoreDir];
		
		scene.markGrid(line.cr, line.cc);
		scene.markGrid(nr, nc);
			
		line.cr = nr;
		line.cc = nc;
		line.cd = highScoreDir;

		return true;
	}
	
	public int getDirection(int d) {
		return direction[d];
	}
	
}
