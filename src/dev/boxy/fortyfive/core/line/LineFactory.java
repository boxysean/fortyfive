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
	
	public final static int 		DEFAULT_STEP_SPEED 		= 1;
	public final static int 		DEFAULT_DRAW_SPEED 		= 1;
	
	protected SceneFactory			sceneFactory;
	
	/**
	 * @defgroup lines lines
	 * 
	 * @{
	 */
	
	/** line name [required] */
	protected String				name;
	
	/** step speed of line [default: 1] */
	protected int					stepSpeed;
	
	/** draw speed of line [default: 1] */
	protected int					drawSpeed;
	
	/** movement of this line [default: this name] */
	protected String				lineMovementName;
	
	/** line draw of this line [default: this name] */
	protected String				lineDrawName;
	
	/** start areas of this line, prefix with "+-.!" (add, subtract, set, unset) */
	protected List<String>			startAreaNames;
	
	/** threshold (non-accessible) areas of this line, prefix with "+-.!" (add, subtract, set, unset) */
	protected List<String>			thresholdNames;
	
	/** coordinate bag of this line [required] */
	protected String				coordBagName;
	
	/** @} */
	
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
		stepSpeed = ConfigParser.getInt(map, "stepSpeed", DEFAULT_STEP_SPEED);
		drawSpeed = ConfigParser.getInt(map, "drawSpeed", DEFAULT_DRAW_SPEED);
		
		lineMovementName = ConfigParser.getString(map, "movement", name);
		lineDrawName = ConfigParser.getString(map, new String[] { "draw", "linedraw" }, name);
		coordBagName = ConfigParser.getString(map, "coordBag", name);
		
		thresholdNames = ConfigParser.getStrings(map, "threshold");
		startAreaNames = ConfigParser.getStrings(map, "startArea");
		
		initThresholds(sceneFactory);
		initStartAreas(sceneFactory);
	}
	
	// TODO move to xyz
	protected void initThresholds(SceneFactory sceneFactory) {
		if (thresholdNames != null) {
			blocked = new boolean[sceneFactory.rows()][sceneFactory.columns()];
			
			for (String thresholdName : thresholdNames) {
				char mod = thresholdName.charAt(0);
				
				if (mod != '-' && mod != '+' && mod != '.' && mod != '!') {
					mod = '+';
				} else {
					thresholdName = thresholdName.substring(1);
				}
				
				Area thresh = sceneFactory.getArea(thresholdName);
				
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
				
				Area area = sceneFactory.getArea(startAreaName);
				
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