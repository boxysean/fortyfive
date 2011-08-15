package dev.boxy.fortyfive.core.line;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.areas.*;
import dev.boxy.fortyfive.core.coordinatebag.*;
import dev.boxy.fortyfive.core.draw.*;
import dev.boxy.fortyfive.core.movement.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.core.startarea.*;
import dev.boxy.fortyfive.utils.*;

public class LineFactory implements ConfigLoader {
	
	public final static double 		DEFAULT_STRAIGHT_PROB 	= 0.80;
	public final static int 		DEFAULT_STEP_SPEED 		= 1;
	public final static int 		DEFAULT_DRAW_SPEED 		= 1;
	
	protected SceneFactory			sceneFactory;
	
	protected String				name;
	protected double				straightProb;
	protected int					stepSpeed;
	protected int					drawSpeed;
	protected String				lineMovementName;
	protected String				lineDrawName;
	protected List<String>			startAreaNames;
	protected List<String>			thresholdNames;
	protected String				coordBagName;
	
	protected List<StartAreaFactory>	startAreaFactories = new ArrayList<StartAreaFactory>();
	protected StartArea				startArea;
	protected boolean[][]			blocked;
	protected int					startAreaIdx = 0;
	
	protected GridLayer				thresholdLayer;
	
	public LineFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public Line get(Scene scene, int br, int bc, int bd) {
		LineMovementFactory lineMovementFactory = scene.getLineMovementFactory(lineMovementName);
		LineDraw lineDraw = scene.getLineDraw(lineDrawName);
		
		Line line = new Line(scene, br, bc, bd, stepSpeed, drawSpeed, lineMovementFactory, lineDraw);
		line.applyBlocked(blocked);
		
		return line;
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		name = ConfigParser.getString(map, "name");
		straightProb = ConfigParser.getDouble(map, "straightProb", DEFAULT_STRAIGHT_PROB);
		stepSpeed = ConfigParser.getInt(map, "stepSpeed", DEFAULT_STEP_SPEED);
		drawSpeed = ConfigParser.getInt(map, "drawSpeed", DEFAULT_DRAW_SPEED);
		
		lineMovementName = ConfigParser.getString(map, "movement");
		lineDrawName = ConfigParser.getString(map, new String[] { "draw", "linedraw" });
		coordBagName = ConfigParser.getString(map, "coordBag");
		
		thresholdNames = ConfigParser.getStrings(map, "threshold");
		startAreaNames = ConfigParser.getStrings(map, "startArea");
		
		initThresholds(sceneFactory);
		initStartAreas(sceneFactory);
	}
	
	// TODO move to xyz
	protected void initThresholds(SceneFactory sceneFactory) {
		if (thresholdNames != null) {
			blocked = new boolean[sceneFactory.rows()][sceneFactory.columns()];
			
			for (String name : thresholdNames) {
				char mod = name.charAt(0);
				
				if (mod != '-' && mod != '+' && mod != '.' && mod != '!') {
					mod = '+';
				} else {
					name = name.substring(1);
				}
				
				Area thresh = null;
				
				if (name.equalsIgnoreCase("all")) {
					thresh = new AllArea();
				} else {
					thresh = sceneFactory.getArea(name);
				}
				
				if (mod == '-') {
					thresh.subtract(blocked);
				} else if (mod == '.') {
					thresh.set(blocked);
				} else if (mod == '!') {
					thresh.unset(blocked);
				} else {
					thresh.add(blocked);
				}
			}
		}
	}
	
	// TODO should this go in the start area?
	public void initStartAreas(SceneFactory sceneFactory) {
		if (startAreaNames != null) {
			boolean[][] startAreaValid = new boolean[sceneFactory.rows()][sceneFactory.columns()];

			for (String startAreaName : startAreaNames) {
				char mod = startAreaName.charAt(0);
				
				if (mod != '-' && mod != '+' && mod != '.' && mod != '!') {
					mod = '+';
				} else {
					startAreaName = startAreaName.substring(1);
				}
				
				Area area = null;
				
				if (startAreaName.equalsIgnoreCase("all")) {
					area = new AllArea();
				} else {
					area = sceneFactory.getArea(startAreaName);
				}
				
				if (mod == '-') {
					area.subtract(startAreaValid);
				} else if (mod == '.') {
					area.set(startAreaValid);
				} else if (mod == '!') {
					area.unset(startAreaValid);
				} else {
					area.add(startAreaValid);
				}
			}
			
			CoordinateBag coordBag = sceneFactory.getCoordinateBag(coordBagName);
			
			StartAreaFactoryJob job = new StartAreaFactoryJob(sceneFactory, startAreaValid, blocked, coordBag, this);
			job.run();
			
			FortyFive ff = FortyFive.getInstance();
			
			for (int i = 0; i < 9; i++) {
				job = new StartAreaFactoryJob(sceneFactory, startAreaValid, blocked, coordBag, this);
				ff.addJob(job);
			}
		}
	}
	
	public void newScene() {
		startArea = getStartAreaFactory().get();
	}
	
	public void addStartAreaFactory(StartAreaFactory startAreaFactory) {
		startAreaFactories.add(startAreaFactory);
	}
	
	public StartAreaFactory getStartAreaFactory() {
		if (++startAreaIdx >= startAreaFactories.size()) {
			startAreaIdx -= startAreaFactories.size();
		}
		
		return startAreaFactories.get(startAreaIdx);
	}
	
	public String getName() {
		return name;
	}
	
	public StartArea getStartArea() {
		return startArea;
	}
	
	public GridLayer getThresholdLayer() {
		if (thresholdLayer == null) {
			thresholdLayer = new GridLayer(sceneFactory, blocked);
		}
		
		return thresholdLayer;
	}
	
	public int getDirection(int d) {
		return sceneFactory.getLineMovementFactory(lineMovementName).getDirection(d);
	}
	
	public GridLayer getStartAreaLayer() {
		return startArea.getGridLayer();
	}

}