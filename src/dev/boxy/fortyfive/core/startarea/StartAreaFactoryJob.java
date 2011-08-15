package dev.boxy.fortyfive.core.startarea;

import java.util.*;

import dev.boxy.fortyfive.core.coordinatebag.*;
import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.scene.*;

public class StartAreaFactoryJob implements Runnable {
	
	protected SceneFactory sceneFactory;
	protected boolean[][] valid;
	protected boolean[][] blocked;
	protected CoordinateBag coordBag;
	protected LineFactory lineFactory;

	protected Set<Coordinate> coordsSet = new TreeSet<Coordinate>();

	public StartAreaFactoryJob(SceneFactory sceneFactory, boolean[][] valid, boolean[][] blocked, CoordinateBag coordBag, LineFactory lineFactory) {
		this.sceneFactory = sceneFactory;
		this.valid = valid;
		this.blocked = blocked;
		this.coordBag = coordBag;
		this.lineFactory = lineFactory;
	}
	
	public void run() {
		coordsSet.clear();
		
		int rows = valid.length;
		int columns = valid[0].length;
		
		// Start with anything not blocked
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				if (valid[r][c] && (blocked == null || !blocked[r][c])) {
					coordsSet.add(new Coordinate(r, c));
				}
			}
		}
		
		// Transfer to a list rather than set
		
		List<Coordinate> coordinates = new ArrayList<Coordinate>();
		coordinates.addAll(coordsSet);
		
		// Order the coordinates in a pre-defined way
		
		coordBag.initList(coordinates);
		
		// Create a factory to represent the work done
		
		lineFactory.addStartAreaFactory(new StartAreaFactory(sceneFactory, coordinates));
	}

}