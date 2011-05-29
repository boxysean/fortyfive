package dev.boxy.fortyfive;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.*;

import processing.core.*;
import dev.boxy.fortyfive.coordinatebag.*;
import dev.boxy.fortyfive.draw.*;
import dev.boxy.fortyfive.movement.*;

public class FortyFive extends PApplet {
	
//	public static final String		DEFAULT_SETTING		= "HeartExp.yaml";
	public static final boolean		DEBUG				= Boolean.getBoolean("DEBUG");
	public static final boolean		SHOW_THRESHOLD		= Boolean.getBoolean("THRESHOLD");
	public static final boolean		SHOW_STARTAREA		= Boolean.getBoolean("STARTAREA");
	
	
	public static final int			IMAGE_THRESHOLD_FUDGE_FACTOR = 7; //pixels
	
	
	// 0 = top, 1 = top right, ..., 7 = top left
	public static final int[]	dr		= new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	public static final int[]	dc		= new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	
	LinearPresentation presentation;
	ImageGridCache imageGridCache = new ImageGridCache();
	String currentConfigFile;
	int userDrawSpeedMultiplier = 1;
	
	public enum Settings {
		
		DANSE_MID ("DanseMid.yaml"),
		DANSE_BLACK ("DanseBlack.yaml"),
		DANSE_BLACK_DIAG ("DanseBlackDiag.yaml"),
		DANSE_WHITE ("DanseWhite.yaml"),
//		SUNSET (600, 399, 1, 1, 1, 10, 1, 0, null, "images/P1010475.JPG", "02000200", null, new int[] { 1, 1, 1, 2, 2, 2, 3, 3, 3, 4 }, 3, null),
//		SMALL (200, 200, 20, 20, 1, 1, 1, 2, null, null, null, null, null, 1, null),
		EYE ("Eye.yaml"),
//		VANCOUVER (480, 640, 2, 2, 0.98, 10, 2, 1, null, "images/vancouver.jpg", null, null, new int[] { 2, 2, 2, 2, 2, 2, 2, 2, 8, 8 }, 1, null),
		BRIDGE ("Bridge.yaml"),
//		KETTLE (600, 800, 2, 2, 1, 5, 1, 0, null, "images/20110114/kettle.jpg", null, null, null, 1, null),
//		RAVEN (600, 800, 6, 6, 0.75, 5, 2, 1, null, "images/20110114/raven.jpg", null, new int[] { 2, 2, 2, 2, 2 }, new int[] { 1, 2, 3, 4, 5 }, 1, null),
//		HELICOPTER (600, 800, 10, 10, 0.75, 5, 2, 2, null, "images/20110114/helicopter.jpg", "20002000", null, null, 1, null),
		DALAI ("Dalai.yaml"),
		HEART ("Heart.yaml"),
		HEART_EXP ("HeartExp.yaml"),
//		HEART_SMALL (450, 750, 2, 2, 1.00, 1, 1, 2, "images/20110213/heart-small.jpg", null, "20002000", null, null, 100, null),
//		MONKEY (800, 600, 2, 2, 0.925, 5, 1, IntelligenceMovement.INTELLIGENCE_NONE, "images/20110220/monkey.jpg", "images/20110220/monkey.jpg", "12121212", null, null, 20, null),
//		MONKEY_BIG (1200, 900, 2, 2, 1.0, 5, 1, 2, "images/20110220/monkey-big.jpg", "images/20110220/monkey-big.jpg", "22222222", null, null, 20, null),
//		RODRIGO_Y_JULIA (1024, 576, 2, 2, 1.0, 5, 1, IntelligenceMovement.INTELLIGENCE_AVOID_ADV, null, "images/20110227/rodrigoyjulia.jpg", "12100121", null, null, 20, null),
		MONO ("Mono.yaml"),
		START_FROM_SIDES ("StartFromSides.yaml"),
		LEFT_FLOW ("LeftFlow.yaml"),
		RAINBOW_FLOW ("RainbowFlow.yaml"),
		;
		
		String	configFile;
		
		private Settings(String configFile) {
			this.configFile = configFile;
		}		
	}
	
	class ConfigParser {
		
		public ConfigParser(File yamlFile, FortyFive ff) throws Exception {
			Yaml yaml = new Yaml();
			
			TimingUtils.mark("config start");
			
			Map<String, Object> map = (Map<String, Object>) yaml.load(new FileReader(yamlFile));
			
			ff.width = getInt(map, "width");
			ff.height = getInt(map, "height");
			
			// Parse background colour TODO make better
			
			String bgColour = getString(map, "bgcolour", "white");
			
			if (bgColour.equalsIgnoreCase("black")) {
				ff.background(0);
				ff.color(0);
				ff.fill(0);
				ff.rect(0, 0, ff.width, ff.height);
			} else if (bgColour.equalsIgnoreCase("white")) {
				ff.background(255);
				ff.color(255);
				ff.fill(255);
				ff.rect(0, 0, ff.width, ff.height);
			}
			
			TimingUtils.mark("resize");

			ff.size(width, height);

			TimingUtils.mark("resize");

			ff.widthSpacing = getInt(map, "widthSpacing");
			ff.heightSpacing = getInt(map, "heightSpacing");
			
			int frameRate = getInt(map, "frameRate", 30);
			ff.frameRate(frameRate);
			
			TimingUtils.mark("master start area");
			
			ff.masterStartArea = new StartArea(ff, new RandomBag());
			ff.masterStartArea.addRectangle(0, 0, width, height);
			
			TimingUtils.mark("master start area");
			
			ff.drawSpeedMultiplier = getInt(map, "drawSpeedMultiplier", 1);
			
			TimingUtils.mark("config start");
			
			// Parse image grids
			
			TimingUtils.mark("parse image grids");
			
			List<Map<String, Object>> imageDefList = (List<Map<String, Object>>) map.get("images");
			
			ff.imageGridMap = new HashMap<String, ImageGrid>();
			
			if (imageDefList != null) {
				for (Map<String, Object> imageDef : imageDefList) {
					String name = (String) imageDef.get("name");
					String imageFile = (String) imageDef.get("file");
					
					ff.imageGridMap.put(name, imageGridCache.get(ff, name, imageFile, yamlFile.getAbsolutePath()));
				}
			}
			
			TimingUtils.mark("parse image grids");
			TimingUtils.print("load image");
			TimingUtils.print("populate colour grid");
			
			// Parse line templates
			
			List<Map<String, Object>> lineTemplateList = (List<Map<String, Object>>) map.get("lines");
			
			ff.nLines = lineTemplateList.size();
			ff.lineTemplates = new LineTemplate[ff.nLines];
			ff.lines = new Line[ff.nLines];
			
			int index = 0;
			
			TimingUtils.mark("parse line template total");
			
			for (Map<String, Object> lineTemplateDef : lineTemplateList) {
				double straightProb = getDouble(lineTemplateDef, "straightProb", LineTemplate.DEF_STRAIGHT_PROB);
				int stepSpeed = getInt(lineTemplateDef, "stepSpeed", LineTemplate.DEF_STEP_SPEED);
				int drawSpeed = getInt(lineTemplateDef, "drawSpeed", LineTemplate.DEF_DRAW_SPEED);
				
				// Parse movement parameters
				
				Map<String, Object> movementDef = (Map<String, Object>) lineTemplateDef.get("movement");
				
				LineMovement movement = null;
				
				if (movementDef != null) {
					String movementName = (String) movementDef.get("name");
					
					if (movementName != null) {
						if (movementName.equals("ClingMovement")) {
							movement = new ClingMovement(ff, null);
						} else if (movementName.equals("IntelligentMovement")) {
							int movementIntelligence = getInt(movementDef, "intelligence", 2);
							movement = new IntelligentMovement(ff, null, movementIntelligence);
						}
					}
				}
				
				if (movement == null) {
					movement = new IntelligentMovement(ff, null, 2);
				}
				
				// Parse direction string
				
				String directionStr = (String) lineTemplateDef.get("direction");
				int direction[] = new int[8];
				Arrays.fill(direction, 2);
				
				if (directionStr != null) {
					for (int i = 0; i < min(directionStr.length(), direction.length); i++) {
						direction[i] = (int) directionStr.charAt(i) - '0';
					}
				}
				
				// Parse draw method
				
				TimingUtils.markAdd("parse draw");
				
				Map<String, Object> drawDef = (Map<String, Object>) lineTemplateDef.get("draw");
				
				LineDraw draw = null;
				
				if (drawDef != null) {
					String drawName = getString(drawDef, "name", "SolidDraw");
					
					if (drawName.equals("SolidDraw")) {
						int red = getInt(drawDef, "red", 0);
						int green = getInt(drawDef, "green", 0);
						int blue = getInt(drawDef, "blue", 0);
						int strokeWidth = getInt(drawDef, "strokeWidth", 1);
						draw = new SolidDraw(red, green, blue, strokeWidth);
					} else if (drawName.equals("ImageDraw")) {
						int strokeWidth = getInt(drawDef, "strokeWidth", 0);
						String image = (String) drawDef.get("image");
						
						int xOffset = getInt(drawDef, "xOffset", 0);
						int yOffset = getInt(drawDef, "yOffset", 0);
						double scale = getDouble(drawDef, "scale", 1.0);
						
						ImageGrid imageGrid = ff.imageGridMap.get(image);
						
						if (scale < 0) {
							int imageWidth = imageGrid.colourPic.width;
							int imageHeight = imageGrid.colourPic.height;
							
							double widthResizeRatio = (double) width / imageWidth;
							double heightResizeRatio = (double) height / imageHeight;
							
							if (scale < -5) {
								scale = Math.max(widthResizeRatio, heightResizeRatio);
							} else {
								scale = Math.min(widthResizeRatio, heightResizeRatio);
							}
						}
						
						if (xOffset < 0) {
							// Center the image along x
							xOffset = (int) (width - (imageGrid.colourPic.width * scale)) / 2;
						}
						
						if (yOffset < 0) {
							// Center the image along y
							yOffset = (int) (height - (imageGrid.colourPic.height * scale)) / 2;
						}
						
						draw = new ImageDraw(imageGrid, strokeWidth, xOffset, yOffset, scale);
					}
				}
				
				if (draw == null) {
					draw = new SolidDraw(0, 0, 0, 1);
				}
				
				TimingUtils.markAdd("parse draw");

				// Parse threshold images
				
				TimingUtils.markAdd("threshold images");
				
				List<Map<String, Object>> thresholdDefs = (List<Map<String, Object>>) lineTemplateDef.get("threshold");
				
				List<ImageThreshold> thresholds = new LinkedList<ImageThreshold>();
				StartArea startArea = null;
				
				if (thresholdDefs != null) {
					for (Map<String, Object> thresholdDef : thresholdDefs) {
						String thresholdName = (String) thresholdDef.get("name");
						boolean thresholdInvert = getBoolean(thresholdDef, "invert", false);
						int thresholdXOffset = getInt(thresholdDef, "xOffset", 0);
						int thresholdYOffset = getInt(thresholdDef, "yOffset", 0);
						double thresholdScale = getDouble(thresholdDef, "scale", 1.0);
						
						ImageGrid thresholdImage = ff.imageGridMap.get(thresholdName);
						
						if (thresholdScale < 0) {
							// Auto scale to size of screen
							int imageWidth = thresholdImage.colourPic.width;
							int imageHeight = thresholdImage.colourPic.height;
							
							double widthResizeRatio = (double)  width / imageWidth;
							double heightResizeRatio = (double) height / imageHeight;
							
							if (thresholdScale < -5) {
								// Scale clips off bigger edges
								thresholdScale = Math.max(widthResizeRatio, heightResizeRatio);
							} else {
								// Scale does not clip
								thresholdScale = Math.min(widthResizeRatio, heightResizeRatio);
							}
						}
						
						if (thresholdXOffset < 0) {
							// Center the image along x
							thresholdXOffset = (int) (width - (thresholdImage.colourPic.width * thresholdScale)) / 2;
						}
						
						if (thresholdYOffset < 0) {
							// Center the image along x
							thresholdYOffset = (int) (height - (thresholdImage.colourPic.height * thresholdScale)) / 2;
						}
						
						thresholds.add(new ImageThreshold(thresholdName, thresholdImage, thresholdInvert, thresholdXOffset, thresholdYOffset, thresholdScale));
					}
				}
				
				TimingUtils.markAdd("threshold images");
				
				// Parse start area
				
				TimingUtils.markAdd("start area");
				
				// First see if a coordinate bag is defined
				
				CoordinateBag coordBag = null;
				
				String coordBagName = getString(lineTemplateDef, "coordBag");
				
				if (coordBagName != null) {
					if (coordBagName.equalsIgnoreCase("ordered") || coordBagName.equalsIgnoreCase("forward")) {
						coordBag = new OrderedBag();
					} else if (coordBagName.equalsIgnoreCase("backward")) {
						coordBag = new OrderedBag(false);
					}
				}
				
				if (coordBag == null) {
					coordBag = new RandomBag();
				}
				
				// Okay, now start initializing the start area
				
				List<Map<String, Object>> startAreaDefs = (List<Map<String, Object>>) lineTemplateDef.get("startArea");
				
				if (startAreaDefs != null) {
					startArea = new StartArea(ff, coordBag);
					
					for (Map<String, Object> startAreaDef : startAreaDefs) {
						if (startAreaDef.containsKey("addRect")) {
							List<Object> coords = (List<Object>) startAreaDef.get("addRect");
							
							int x = parseInt(coords, 0, ff.width, 0);
							int y = parseInt(coords, 1, ff.height, 0);
							int width = parseInt(coords, 2, ff.width, ff.width);
							int height = parseInt(coords, 3, ff.height, ff.height);
							
							startArea.addRectangle(x, y, width, height);
						}
						
						if (startAreaDef.containsKey("addImage")) {
							List<Object> image = (List<Object>) startAreaDef.get("addImage");
							
							// Allow some fudge factor in the config for extra flexibility. Positive offsets shrink the side.
							
							String imageName = (String) image.get(0);
							int topOffset = getInt(image, 1, 0);
							int rightOffset = getInt(image, 2, 0);
							int bottomOffset = getInt(image, 3, 0);
							int leftOffset = getInt(image, 4, 0);
							
							// Go through each existing threshold and add this images' area
							
							for (ImageThreshold imageThreshold : thresholds) {
								if (imageThreshold.image.name.equals(imageName)) {
									int x = imageThreshold.xOffset + leftOffset;
									int y = imageThreshold.yOffset + topOffset;
									double scale = imageThreshold.scale;
									int width = (int) (imageThreshold.image.colourPic.width * scale) - leftOffset - rightOffset;
									int height = (int) (imageThreshold.image.colourPic.height * scale) - topOffset - bottomOffset;
									
									startArea.addRectangle(x + IMAGE_THRESHOLD_FUDGE_FACTOR, y + IMAGE_THRESHOLD_FUDGE_FACTOR, width - IMAGE_THRESHOLD_FUDGE_FACTOR - IMAGE_THRESHOLD_FUDGE_FACTOR, height - IMAGE_THRESHOLD_FUDGE_FACTOR - IMAGE_THRESHOLD_FUDGE_FACTOR);
								}
							}
						}
						
						if (startAreaDef.containsKey("removeRect")) {
							List<Object> coords = (List<Object>) startAreaDef.get("removeRect");
							
							int x = parseInt(coords, 0, ff.width, 0);
							int y = parseInt(coords, 1, ff.height, 0);
							int width = parseInt(coords, 2, ff.width, ff.width);
							int height = parseInt(coords, 3, ff.height, ff.height);
							
							startArea.removeRectangle(x, y, width, height);
						}
						
						if (startAreaDef.containsKey("removeImage")) {
							List<Object> image = (List<Object>) startAreaDef.get("removeImage");
							
							String imageName = (String) image.get(0);
							int topOffset = getInt(image, 1, 0);
							int rightOffset = getInt(image, 2, 0);
							int bottomOffset = getInt(image, 3, 0);
							int leftOffset = getInt(image, 4, 0);
							
							// Go through each existing threshold and remove the images' area
							
							for (ImageThreshold imageThreshold : thresholds) {
								if (imageThreshold.image.name.equals(imageName)) {
									int x = imageThreshold.xOffset + leftOffset;
									int y = imageThreshold.yOffset + topOffset;
									double scale = imageThreshold.scale;
									int width = (int) (imageThreshold.image.colourPic.width * scale) - leftOffset - rightOffset;
									int height = (int) (imageThreshold.image.colourPic.height * scale) - topOffset - bottomOffset;
									
									startArea.removeRectangle(x - IMAGE_THRESHOLD_FUDGE_FACTOR, y - IMAGE_THRESHOLD_FUDGE_FACTOR, width + IMAGE_THRESHOLD_FUDGE_FACTOR + IMAGE_THRESHOLD_FUDGE_FACTOR, height + IMAGE_THRESHOLD_FUDGE_FACTOR + IMAGE_THRESHOLD_FUDGE_FACTOR);
								}
							}
						}
					}
				}
				
				if (startArea == null) {
					if (coordBag == null) {
						startArea = masterStartArea;
					} else {
						startArea = new StartArea(ff, coordBag);
						startArea.addRectangle(0, 0, width, height);
					}
				}
				
				startArea.commitCoords(thresholds);
				
				TimingUtils.markAdd("start area");

				// Done!

				ff.lineTemplates[index++] = new LineTemplate(straightProb, stepSpeed, drawSpeed, movement, direction, draw, startArea, thresholds);
			}
			
			TimingUtils.mark("parse line template total");
			TimingUtils.print("parse draw");
			TimingUtils.print("start area");
			TimingUtils.print("threshold images");
		}
		
		public int getInt(Map<String, Object> map, String key) {
			Integer i = (Integer) map.get(key);
			return i.intValue();
		}
		
		public int getInt(Map<String, Object> map, String key, int def) {
			if (map.containsKey(key)) {
				return getInt(map, key);
			} else {
				return def;
			}
		}
		
		public int getInt(List<Object> list, int idx, int def) {
			if (list.size() <= idx) {
				return def;
			} else {
				return ((Integer) list.get(idx)).intValue();
			}
		}
		
		public double getDouble(Map<String, Object> map, String key) {
			Object o = map.get(key);
			
			if (o instanceof Integer) {
				Integer d = (Integer) o;
				return d.intValue();
			} else {
				Double d = (Double) map.get(key);
				return d.doubleValue();
			}
		}
		
		public double getDouble(Map<String, Object> map, String key, double def) {
			if (map.containsKey(key)) {
				return getDouble(map, key);
			} else {
				return def;
			}
		}
		
		public boolean getBoolean(Map<String, Object> map, String key) {
			Boolean b = (Boolean) map.get(key);
			return b.booleanValue();
		}
		
		public boolean getBoolean(Map<String, Object> map, String key, boolean def) {
			if (map.containsKey(key)) {
				return getBoolean(map, key);
			} else {
				return def;
			}
		}
		
		public String getString(Map<String, Object> map, String key, String def) {
			if (map.containsKey(key)) {
				return getString(map, key);
			} else {
				return def;
			}
		}
		
		public String getString(Map<String, Object> map, String key) {
			return (String) map.get(key);
		}
		
		public int parseInt(List<Object> list, int idx, int n, int def) {
			try {
				Object xObj = list.get(idx);
				
				int x = 0;
				
				if (xObj instanceof Integer) {
					x = ((Integer) xObj).intValue();
				} else if (xObj instanceof String) {
					String xStr = (String) xObj;
					
					if (xStr.matches("^-?[0-9]*\\/\\s*-?[0-9]\\+$")) {
						// If it's any ratio form, then multiply the ratio by the length / width / etc
						
						String tokens[] = xStr.split("/");
						
						int numerator;
						int denominator;
						
						if (tokens.length == 1) {
							numerator = 1;
							denominator = Integer.parseInt(tokens[0]);
						} else {
							numerator = Integer.parseInt(tokens[0]);
							denominator = Integer.parseInt(tokens[1]);
						}
						
						x = (int) ((double) numerator / denominator * n);
					} else {
						x = Integer.parseInt(xStr);
					}
				}
				
				if (x < 0) {
					x += n;
				}
				
				return x;
			} catch (Exception e) {
				return def;
			}
		}
	}
	
	int				width				= 0;
	int				height				= 0;
	
	public int		widthSpacing		= 0;
	public int		heightSpacing		= 0;
	
	int				nLines 				= 1;
	LineTemplate[]	lineTemplates		= null;
	
	int				drawSpeedMultiplier	= 1;
	
	StartArea		masterStartArea;
	
	public boolean[][]		grid;
	
	List<Integer>			dlist	= new LinkedList<Integer>();
	
	PImage						thresholdPic;
	HashMap<String, ImageGrid>	imageGridMap;
	
	Line[]	lines	= new Line[nLines];
	
	boolean	pause	= false;
	
	public void loadSettings(String configFile) throws Exception {
		ConfigParser cp = new ConfigParser(new File("../configs/" + configFile), this);
	}
	
	@Override
	public void setup() {
		noCursor();
		
		presentation = new LinearPresentation(this);
		
		addKeyListener(presentation);
		
		setup(presentation.getCurrentFile());
	}
	
	String queuedConfig = null;
	
	public void queueConfig(String configFile) {
		queuedConfig = configFile;
	}
	
	/**
	 * Should only be called by this class, otherwise exception thrown.
	 * @param configFile
	 */
	private void setup(String configFile) {
		TimingUtils.reset();
		
		System.out.println("--- " + configFile + " ---");
		
		TimingUtils.mark("setup");
		
		TimingUtils.mark("load settings");
		
		
		try {
			loadSettings(configFile);
		} catch (Exception e) {
			// The presentation object takes care of the exception and we should try setting up again
			e.printStackTrace();
			
			presentation.onLoadFail();
			return;
		}
		
		presentation.loadFails = 0;
			
		currentConfigFile = configFile;
		
		TimingUtils.mark("load settings");
		
		// Create a list of coordinates that have not yet been visited
		
		grid = new boolean[rows()][columns()];
		
		// Create a list of directions that can be shuffled when lines are traversing
		
		dlist.clear();
		
		for (int i = 0; i < 8; i++) {
			dlist.add(i);
		}
		
		Collections.shuffle(dlist);
		
		// Create the lines
		
		TimingUtils.mark("new lines");
		
		for (int i = 0; i < nLines; i++) {
			lines[i] = newLine(lineTemplates[i]);
		}
		
		TimingUtils.mark("new lines");
		
		TimingUtils.mark("setup");
	}
	
	@Override
	public void draw() {
		if (queuedConfig != null) {
			// New config has been requested
			setup(queuedConfig);
			queuedConfig = null;
		}
		
		if (!pause) {
			stroke(0);
			
			boolean finished = true;
			
			for (int i = 0; i < nLines; i++) {
				Line line = lines[i];
				
				int multiplier = userDrawSpeedMultiplier;
				
				if (line != null) {
					for (int j = 0; j < line.drawSpeed * multiplier; j++) {
						if (!line.forwardDraw()) {
							line = newLine(lineTemplates[i]);
							lines[i] = line;
						}
						
						if (line == null) {
							break;
						}
						
						finished = false;
					}
				}
			}
			
			if (finished) {
				presentation.onFinished();
			}
		}
	}
	
	public void keyTyped(KeyEvent e) {
		char key = e.getKeyChar();
		
		if ('0' <= key && key <= '9') {
			userDrawSpeedMultiplier = key - '0';
		}
		
		switch (key) {
		case ' ':
			pause = !pause;
			break;
			
		case 'c':
			reset();
			break;
		}
	}
	
	public void keyPressed(KeyEvent e) {
	    int keyCode = e.getKeyCode();
	    
		switch (keyCode) {
		case KeyEvent.VK_UP:
			userDrawSpeedMultiplier++;
			
			break;
			
		case KeyEvent.VK_DOWN:
			userDrawSpeedMultiplier--;
			if (/*drawSpeedMultiplier + */userDrawSpeedMultiplier < 1) {
				userDrawSpeedMultiplier = 1;
			}
			
			break;
		}
	}
	
	public void reset() {
		queueConfig(currentConfigFile);
	}
	
	public void drawLine(int gr, int gc, int grr, int gcc, LineDraw draw) {
		float px = gridToPixel(gc, columns(), width);
		float py = gridToPixel(gr, rows(), height);
		float pxx = gridToPixel(gcc, columns(), width);
		float pyy = gridToPixel(grr, rows(), height);
		
		draw.drawLine(this, gr, gc, grr, gcc, px, py, pxx, pyy);
	}
	
	public void drawBox(int gr, int gc) {
		drawBox(gr, gc, color(255, 225, 225));
	}
	
	public void drawBox(int gr, int gc, int color) {
		float px = gridToPixel(gc, columns(), width) - (widthSpacing / 2);
		float py = gridToPixel(gr, rows(), height) - (heightSpacing / 2);
		
		noStroke();
		fill(color);
		rect(px, py, widthSpacing, heightSpacing);
	}
	
	/** 
	 * Draw a box from (gr, gc) to (gr + rows, gc + columns)
	 * @param px leftmost row
	 * @param py leftmost row
	 * @param width rows in box
	 * @param height columns in box
	 */
	public void drawBox(int px, int py, int width, int height) {
		float pxx = px + width;
		float pyy = py + height;
		
		noStroke();
		fill(color(255, 225, 225));
		rect(px, py, pxx, pyy);
	}
	
	/**
	 * Given a grid coordinate along an axis, return the pixel coordinate halfway to the grid coordinate below and to its right.
	 * @param gg grid coordinate along axis
	 * @param gmax grid axis max
	 * @param smax pixel axis max
	 * @return pixel coordinate along axis
	 */
	public float gridToPixel(int gg, int gmax, int smax) {
		return (2 * gg + 1) * ((float) smax / gmax) / 2.0f;
	}
	
	/**
	 * Given a pixel along an axis, return the "floor" grid coordinate, i.e., the grid coordinate up and left from the pixel.  
	 * @param pp pixel along axis
	 * @param pmax pixel axis max
	 * @param gmax grid axis max
	 * @return floor grid coordinate along axis
	 */
	public int pixelToGrid(float pp, float pmax, float gmax) {
		return (int) (pp * gmax / pmax);
	}
	
	/**
	 * Given the grid column, return the x pixel space coordinate
	 * @param c grid column
	 * @return x pixel space coordinate
	 */
	public float columnToX(int c) {
		return gridToPixel(c, columns(), width);
	}
	
	/**
	 * Given the x pixel space coordinate, return the grid column
	 * @param x x pixel space coordinate 
	 * @return grid column
	 */
	public int xToColumn(float x) {
		return pixelToGrid(x, width, columns());
	}
	
	/**
	 * Given the grid row, return the y pixel space coordinate
	 * @param r grid row
	 * @return y pixel space coordinate
	 */
	public float rowToY(int r) {
		return gridToPixel(r, rows(), height);
	}
	
	/**
	 * Given the y pixel space coordinate, return the grid row
	 * @param y y pixel space coordinate
	 * @return grid row
	 */
	public int yToRow(float y) {
		return pixelToGrid(y, height, rows());
	}
	
	public Line newLine(LineTemplate lineTemplate) {
		int gr = 0;
		int gc = 0;
		int gd = -1;
		
		// This loops will break when there are no more places to try and place lines.
		
		while (gd == -1) {
			StartArea startArea = lineTemplate.startArea; 
			
			// If there is no start point, we cannot create this line.
			
			if (!startArea.getNextStartPoint(lineTemplate.thresholds)) {
				return null;
			}
			
			gc = startArea.getStartColumn();
			gr = startArea.getStartRow();
			
			// drawBox(gr, gc);
			
			// Pick a random direction; the first highest directional value that does not make an illegal move. 
			
			int highestValue = -1;
			int highestDirection = -1;
			
			for (int d : dlist) {
				if (lineTemplate.direction[d] > highestValue) {
					int nr = gr + dr[d];
					int nc = gc + dc[d];
					
					if (!invalidMove(gr, gc, nr, nc)) {
						highestValue = lineTemplate.direction[d];
						highestDirection = d;
					}
				}
			}
			
			gd = highestDirection;
			
			Collections.shuffle(dlist);
		}
		
		return new Line(gr, gc, gd, this, lineTemplate);
	}
	
	/**
	 * Check if the proposed move runs into or crosses existing lines
	 * @param cr current row
	 * @param cc current column
	 * @param nr new row
	 * @param nc new column
	 * @return true if the move is invalid
	 */
	public boolean invalidMove(int cr, int cc, int nr, int nc) {
		return invalidMove(cr, cc, nr, nc, null);
	}

	/**
	 * Check if the proposed move runs into or crosses existing lines
	 * @see invalidMove(int, int, int, int)
	 * @param cr current row
	 * @param cc current column
	 * @param nr new row
	 * @param nc new column
	 * @param blocked secondary per-line blocking, for example threshold images applied
	 * @return true if the move is invalid
	 */
	public boolean invalidMove(int cr, int cc, int nr, int nc, boolean[][] blocked) {
		boolean invalid = nr < 0 || nc < 0 || nr >= rows() || nc >= columns() || grid[nr][nc] || (blocked != null && blocked[nr][nc]);
		
		if (invalid) {
			return invalid;
		}
		
		invalid |= cr != nr && cc != nc && (grid[nr][cc] || (blocked != null && blocked[nr][cc])) && (grid[cr][nc] || (blocked != null && blocked[cr][nc]));
		
		return invalid;
	}
	
	/**
	 * Give the number of rows in the main workspace
	 * @return rows in the main workspace
	 */
	public int rows() {
		return rows(height);
	}
	
	/**
	 * Give the number of rows in the workspace given the height
	 * @param height height of the workspace
	 * @return number of rows in the workspace given the height
	 */
	public int rows(int height) {
		return height / heightSpacing;
	}
	
	/**
	 * Give the number of columns in the main workspace
	 * @return columns in the main workspace
	 */
	public int columns() {
		return columns(width);
	}
	
	/**
	 * Give the number of columns in the workspace given the width
	 * @param width width of the workspace
	 * @return number of columns in the workspace given the width
	 */
	public int columns(int width) {
		return width / widthSpacing;
	}
	
	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "dev.boxy.fortyfive.FortyFive" });
//		PApplet.main(new String[] { "dev.boxy.fortyfive.FortyFive" });
		
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter("/tmp/tst"));
			out.println("het! " + System.currentTimeMillis());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
}
