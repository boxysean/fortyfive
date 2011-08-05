package dev.boxy.fortyfive.core.scene;

import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.*;

import dev.boxy.fortyfive.core.colour.*;
import dev.boxy.fortyfive.core.coordinatebag.*;
import dev.boxy.fortyfive.core.draw.*;
import dev.boxy.fortyfive.core.image.*;
import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.movement.*;
import dev.boxy.fortyfive.core.startarea.*;
import dev.boxy.fortyfive.utils.*;

public class SceneFactory extends SceneGeometry {
	
	protected List<String> lineNames;
	protected String bgColour;
	protected int frameRate;
	protected int drawSpeedMultiplier;
	
	protected Map<String, ColourFactory> colourFactories = new LinkedHashMap<String, ColourFactory>();
	protected Map<String, ColourPaletteFactory> colourPaletteFactories = new LinkedHashMap<String, ColourPaletteFactory>();
	protected Map<String, CoordinateBag> coordinateBags = new LinkedHashMap<String, CoordinateBag>();
	protected Map<String, LineDrawFactory> lineDrawFactories = new LinkedHashMap<String, LineDrawFactory>();
	protected Map<String, ImageGrid> imageGrids = new LinkedHashMap<String, ImageGrid>();
	protected Map<String, ImageThreshold> imageThresholds = new LinkedHashMap<String, ImageThreshold>();
	protected Map<String, LineFactory> lineFactories = new LinkedHashMap<String, LineFactory>();
	protected Map<String, LineMovementFactory> lineMovementFactories = new LinkedHashMap<String, LineMovementFactory>();
	protected Map<String, StartAreaFactory> startAreaFactories = new LinkedHashMap<String, StartAreaFactory>();
	
	
	public SceneFactory(Map<String, Object> map) {
		super(0, 0);
		loadSettings(map);
	}
	
	public Scene get() {
		return new Scene(this, new ArrayList<ColourFactory>(colourFactories.values()), new ArrayList<ColourPaletteFactory>(colourPaletteFactories.values()),
				new ArrayList<LineDrawFactory>(lineDrawFactories.values()), new ArrayList<StartAreaFactory>(startAreaFactories.values()),
				lineNames, bgColour, widthSpacing, heightSpacing, frameRate, drawSpeedMultiplier);
	}
	
	public void loadSettings(Map<String, Object> map) {
		// Parse background colour TODO make better, use colour palettes!
		
		bgColour = ConfigParser.getString(map, "bgcolour", "white");
		widthSpacing = ConfigParser.getInt(map, "widthSpacing");
		heightSpacing = ConfigParser.getInt(map, "heightSpacing");
		frameRate = ConfigParser.getInt(map, "frameRate", 30);
		drawSpeedMultiplier = ConfigParser.getInt(map, "drawSpeedMultiplier", 1);
		lineNames = (List<String>) map.get("deploy");

		loadSettings2(map);
	}
	
	public void loadSettings2(Map<String, Object> map) {
		loadIncludes(map);
		
		// Parse colours
		
		List<Map<String, Object>> colourList = (List<Map<String, Object>>) map.get("colours");
		
		if (colourList != null) {
			for (Map<String, Object> colour : colourList) {
				ColourFactory colourFactory = new ColourFactory(this, colour);
				colourFactories.put(colourFactory.getName(), colourFactory);
				ColourPaletteFactory colourPaletteFactory = new ColourPaletteFactory(colourFactory);
				colourPaletteFactories.put(colourPaletteFactory.getName(), colourPaletteFactory);
			}
		}
		
		// Parse colour palettes
		
		List<Map<String, Object>> colourPaletteList = (List<Map<String, Object>>) map.get("colourPalettes");
		
		if (colourPaletteList != null) {
			for (Map<String, Object> colourPalette : colourPaletteList) {
				ColourPaletteFactory colourPaletteFactory = new ColourPaletteFactory(this, colourPalette);
				colourPaletteFactories.put(colourPaletteFactory.getName(), colourPaletteFactory);
			}
		}
		
		// Parse image grids
		
		List<Map<String, Object>> imageDefList = (List<Map<String, Object>>) map.get("images");
		
		if (imageDefList != null) {
			for (Map<String, Object> imageDef : imageDefList) {
				ImageGridFactory imageGridFactory = new ImageGridFactory(this, imageDef);
				ImageGrid imageGrid = imageGridFactory.get();
				imageGrids.put(imageGrid.getName(), imageGrid);
			}
		}
		
		// Parse threshold images
		
		List<Map<String, Object>> thresholdDefs = (List<Map<String, Object>>) map.get("threshold");
		
		if (thresholdDefs != null) {
			for (Map<String, Object> thresholdDef : thresholdDefs) {
				ImageThresholdFactory imageThresholdFactory = new ImageThresholdFactory(this, thresholdDef);
				ImageThreshold imageThreshold = imageThresholdFactory.get();
				imageThresholds.put(imageThreshold.getName(), imageThreshold);
			}
		}
		
		// Parse coordinate bag
		
		List<Map<String, Object>> coordBagDefs = (List<Map<String, Object>>) map.get("coordBags");

		if (coordBagDefs != null) {
			for (Map<String, Object> coordBagDef : coordBagDefs) {
				CoordinateBagFactory coordBagFactory = new CoordinateBagFactory(this, coordBagDef);
				CoordinateBag coordBag = coordBagFactory.get();
				coordinateBags.put(coordBagFactory.getName(), coordBag);
			}
		}
		
		// Parse start areas
		
		List<Map<String, Object>> startAreaDefs = (List<Map<String, Object>>) map.get("startAreas");
		
		if (startAreaDefs != null) {
			for (Map<String, Object> startAreaDef : startAreaDefs) {
				VectorStartAreaFactory vectorStartAreaFactory = new VectorStartAreaFactory(this, startAreaDef);
				StartAreaFactoryJob job = vectorStartAreaFactory.get();
				StartAreaFactory startAreaFactory = job.run();
				startAreaFactories.put(startAreaFactory.getName(), startAreaFactory);
			}
		}
		
		// Parse draw methods
		
		List<Map<String, Object>> drawDefs = (List<Map<String, Object>>) map.get("lineDraws");
		
		if (drawDefs != null) {
			for (Map<String, Object> drawDef : drawDefs) {
				String name = ConfigParser.getString(drawDef, "name");
				String type = ConfigParser.getString(drawDef, new String[] { "type", "mode" }, "SolidDraw");
				LineDrawFactory lineDrawFactory = null;
				
				if (type.toLowerCase().startsWith(("solid"))) {
					lineDrawFactory = new SolidDrawFactory(this, drawDef);
				} else if (type.toLowerCase().equals("image")) {
					lineDrawFactory = new ImageDrawFactory(this, drawDef);
				} else {
					Logger.getInstance().warning("unknown draw type %s", type);
				}
				
				lineDrawFactories.put(lineDrawFactory.getName(), lineDrawFactory);
			}
		}
		
		// Parse movement methods
		
		List<Map<String, Object>> movementDefs = (List<Map<String, Object>>) map.get("movements");
		
		if (movementDefs != null) {
			for (Map<String, Object> movementDef : movementDefs) {
				LineMovementFactory lineMovementFactory = new LineMovementFactory(this, movementDef);
				lineMovementFactories.put(lineMovementFactory.getName(), lineMovementFactory);
			}
		}
		
		// Parse line templates
		
		List<Map<String, Object>> lineFactoryList = (List<Map<String, Object>>) map.get("lines");
		
		if (lineFactoryList != null) {
			for (Map<String, Object> lineFactoryDef : lineFactoryList) {
				LineFactory lineFactory = new LineFactory(this, lineFactoryDef);
				lineFactories.put(lineFactory.getName(), lineFactory);
			}
		}
	}
	
	public void loadIncludes(Map<String, Object> map) {
		List<String> includes = (List<String>) map.get("includes");
		
		Set<String> loadedFiles = new HashSet<String>();
		
		if (includes != null) {
			for (String include : includes) {
				if (!loadedFiles.contains(include)) {
					Logger.getInstance().log("loading %s", include);
					try {
						Yaml yaml = new Yaml();
						Map<String, Object> includeMap = (Map<String, Object>) yaml.load(new FileReader(include));
						loadedFiles.add(include);
						loadSettings2(includeMap);
					} catch (FileNotFoundException e) {
						Logger.getInstance().warning("loadIncludes: could not load %s because not found, full path %s", include, new File(include).getAbsolutePath());
					}
				}
			}
		}
	}
	
	public ImageGrid getImageGrid(String name) {
		ImageGrid res = imageGrids.get(name);
		
		if (res == null) {
			Logger.getInstance().warning("no such image grid %s", name);
		}

		return res;
	}
	
	public ImageThreshold getImageThreshold(String name) {
		ImageThreshold res = imageThresholds.get(name);
		
		if (res == null) {
			Logger.getInstance().warning("no such image threshold %s", name);
		}
		
		return res;
	}
	
	public List<ImageThreshold> getImageThresholds() {
		return new ArrayList<ImageThreshold>(imageThresholds.values());
	}
	
	public CoordinateBag getCoordinateBag(String name) {
		CoordinateBag res = coordinateBags.get(name);
		
		if (res == null) {
			Logger.getInstance().warning("no such coordinate bag %s", name);
		}
		
		return res;
	}
	
	public LineMovementFactory getLineMovementFactory(String name) {
		LineMovementFactory res = lineMovementFactories.get(name);
		
		if (res == null) {
			Logger.getInstance().warning("no such line movement factory %s", name);
		}
		
		return res;
	}
	
	public LineFactory getLineFactory(String name) {
		LineFactory res = lineFactories.get(name);
		
		if (res == null) {
			Logger.getInstance().warning("no such line factory %s", name);
		}
		
		return res;
	}
	
}
