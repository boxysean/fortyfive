package dev.boxy.fortyfive.core.movement;

import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class IntelligentMovement extends LineMovement {
	
	public static final int		INTELLIGENCE_HIT		= 0;
	public static final int		INTELLIGENCE_NONE		= 1;
	public static final int		INTELLIGENCE_AVOID		= 2;
	public static final int		INTELLIGENCE_AVOID_ADV	= 3;
	
	protected int lineIntelligence;
	
	public IntelligentMovement(Scene scene, Line line, String name, int lineIntelligence) {
		super(scene, line, name);
		
		this.lineIntelligence = lineIntelligence;
	}

	@Override
	public boolean forwardOnce() {
		int nAttempts = 3;
		
		// Don't bother trying any collision avoidance
		
		if (lineIntelligence <= INTELLIGENCE_NONE) {
			nAttempts = 1;
		}
		
		int rotate = 0;
		
		// This is a state machine:
		// On attempt 0, depending on a die roll, attempt to go straight OR skip immediately to attempt 1
		// On attempt 1, attempt to rotate a random direction based on a coin flip
		// On attempt 2, attempt to rotate the other direction
		// Attempt 3 happens if attempt 0 is skipped and is fallen through to
		// Otherwise fail
		
		for (int attempts = 0; attempts < nAttempts; attempts++) {
			int nd = line.cd;
			
			if (attempts == 0) {
				double rand = RandomSingleton.nextDouble();
				
				if (rand >= line.straightProb || line.direction[nd] == DIR_AVOID) {
					// The random choice says we must turn
					attempts++;
					
					// unless turning is bad, then we'll try going straight later.
					if (lineIntelligence >= INTELLIGENCE_AVOID) {
						nAttempts++;
					}
				}
			}
			
			if (attempts == 1) {
				// Rotate one direction on the second attempt
				rotate = RandomSingleton.nextInt(2) * 2 - 1;
				nd += rotate;
			} else if (attempts == 2) {
				// Rotate the opposite direction on the third attempt
				nd -= rotate;
			} else if (attempts == 3) {
				// Finally try going straight if intelligence is high enough
				// nop
			}
			
			if (nd < 0) {
				nd += dr.length;
			} else if (nd >= dr.length) {
				nd -= dr.length;
			}
			
			if (line.direction[nd] == DIR_DISALLOWED) {
				continue;
			}
			
			int nr = line.cr + dr[nd];
			int nc = line.cc + dc[nd];
			
			if (line.invalidMove(nr, nc)) {
				// if lineIntelligence AVOID then attempt to find a new path if the suggested path does not work
				
				if (lineIntelligence >= INTELLIGENCE_AVOID) {
					continue;
				}
				
				break;
			}
			
			// if lineIntelligence is AVOID_ADV then try to prevent choosing a path that will inevitably lead into a dead end
			
			if (attempts < nAttempts-1 && lineIntelligence >= INTELLIGENCE_AVOID_ADV) {
				if (!hasMove(nr, nc, nd)) {
					continue;
				}
			}
			
			scene.markGrid(line.cr, line.cc);
			scene.markGrid(nr, nc);
			
//			drawBox(nr, nc);
			
			line.cr = nr;
			line.cc = nc;
			line.cd = nd;
			
			return true;
		}
		
		return false;
	}
}
