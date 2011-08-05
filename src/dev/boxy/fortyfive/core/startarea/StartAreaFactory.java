package dev.boxy.fortyfive.core.startarea;

import java.util.*;

import dev.boxy.fortyfive.core.scene.*;

public class StartAreaFactory {
	
	protected String name;
	protected SceneFactory sceneFactory;
	protected List<Coordinate> coords;
	
	public StartAreaFactory(String name, SceneFactory sceneFactory, List<Coordinate> coords) {
		this.name = name;
		this.sceneFactory = sceneFactory;
		this.coords = coords;
		
	}
	
	public StartArea get(Scene scene) {
		return new StartArea(scene, name, new ArrayList<Coordinate>(coords));
	}
	
	public String getName() {
		return name;
	}

}