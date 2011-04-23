package dev.boxy.fortyfive.movement;
import java.util.*;

import dev.boxy.fortyfive.*;


public class ClingMovement extends LineMovement {
	
	public List<Integer> dlist = new ArrayList<Integer>();
	
	public ClingMovement(FortyFive ff, Line line) {
		super(ff, line);
		
		dlist.add(-1);
		dlist.add(0);
		dlist.add(1);
	}
	
	@Override
	public LineMovement clone(Line line) {
		return new ClingMovement(ff, line);
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

			if (ff.invalidMove(line.cr, line.cc, nr, nc, line.blocked)) {
				continue;
			}
			
			// Keep score of how many boxes are filled or are outside the graphique
			
			int score = 0;
			
			for (int j = 0; j < 8; j++) {
				int mr = nr + dr[j];
				int mc = nc + dc[j];
				
				if (mr < 0 || mr >= ff.rows() || mc < 0 || mc >= ff.columns() || ff.grid[mr][mc]) {
					score++;
				}
			}
			
			if (score > highScore) {
				highScore = score;
				highScoreDir = nd;
			}
		}
		
		if (highScoreDir < 0) {
			return false;
		}
		
		int nr = line.cr + dr[highScoreDir];
		int nc = line.cc + dc[highScoreDir];
		
		ff.grid[line.cr][line.cc] = true;
		ff.grid[nr][nc] = true;
			
		line.cr = nr;
		line.cc = nc;
		line.cd = highScoreDir;
		
		return true;
	}
}
