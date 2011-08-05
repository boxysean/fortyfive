package dev.boxy.fortyfive.core.startarea;

import java.util.*;

import dev.boxy.fortyfive.core.coordinatebag.*;
import dev.boxy.fortyfive.core.scene.*;

public class StartAreaFactoryJob {
	
	protected SceneFactory sceneFactory;
	
	protected Set<Coordinate> coordsSet = new TreeSet<Coordinate>();

	protected boolean[][] blocked = null;
	protected boolean coordsCommitted = false;
	
	protected String name;
	protected boolean startBlocked = false;
	protected List<StartAreaShape> shapeList = new ArrayList<StartAreaShape>();
	protected String coordBagName;

	public StartAreaFactoryJob(SceneFactory sceneFactory, String name, boolean startBlocked, List<StartAreaShape> shapeList, String coordBagName) {
		this.sceneFactory = sceneFactory;
		this.name = name;
		this.startBlocked = startBlocked;
		this.shapeList.addAll(shapeList);
		this.coordBagName = coordBagName;
	}
	
	public StartAreaFactory run() {
		coordsSet.clear();
		
		int rows = sceneFactory.rows();
		int columns = sceneFactory.columns();
		
		if (blocked == null || blocked.length != rows || blocked[0].length != columns) {
			blocked = new boolean[rows][columns];
		}
		
		// starting blocked?
		
		if (startBlocked) {
			for (boolean[] b : blocked) {
				Arrays.fill(b, true);
			}
		}
		
		// Apply all previously defined rectangles and thresholds
		
		for (StartAreaShape shape : shapeList) {
			shape.apply(blocked);
		}
		
		// Start with anything not blocked
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				if (!blocked[r][c]) {
					coordsSet.add(new Coordinate(r, c));
				}
			}
		}
		
		// Transfer to a list rather than set
		
		List<Coordinate> coordinates = new ArrayList<Coordinate>();
		coordinates.addAll(coordsSet);
		
		// Order the coordinates in a pre-defined way
		
		CoordinateBag coordBag = sceneFactory.getCoordinateBag(coordBagName);
		coordBag.initList(coordinates);
		
		// Create a factory to represent the work done
		
		return new StartAreaFactory(name, sceneFactory, coordinates);
	}

}