package dev.boxy.fortyfive;
import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.*;

import processing.core.*;
import dev.boxy.fortyfive.draw.*;
import dev.boxy.fortyfive.movement.*;

public class FortyFive extends PApplet {
	
	public static final Settings	DEFAULT_SETTING		= Settings.START_FROM_SIDES;
	
	public static final int			MIN_THRESHOLD_VALUE		= 50;
	
	public static final boolean		DEBUG	= Boolean.getBoolean("DEBUG");
	
	// 0 = top, 1 = top right, ..., 7 = top left
	public static final int[]	dr		= new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	public static final int[]	dc		= new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	
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
			
			Map<String, Object> map = (Map<String, Object>) yaml.load(new FileReader(yamlFile));
			
			ff.width = getInt(map, "width");
			ff.height = getInt(map, "height");
			
			ff.widthSpacing = getInt(map, "widthSpacing");
			ff.heightSpacing = getInt(map, "heightSpacing");
			
			ff.masterStartArea = new RectangleArea(ff, 0, 0, ff.width, ff.height);
			
			ff.drawSpeedMultiplier = getInt(map, "drawSpeedMultiplier", 1);
			
			// Parse image grids
			
			List<Map<String, Object>> imageDefList = (List<Map<String, Object>>) map.get("images");
			
			ff.imageGridMap = new HashMap<String, ImageGrid>();
			
			if (imageDefList != null) {
				for (Map<String, Object> imageDef : imageDefList) {
					String name = (String) imageDef.get("name");
					String file = (String) imageDef.get("file");
					
					ff.imageGridMap.put(name, new ImageGrid(ff, file));
				}
			}
			
			// Parse line templates
			
			List<Map<String, Object>> lineTemplateList = (List<Map<String, Object>>) map.get("lines");
			
			ff.nLines = lineTemplateList.size();
			ff.lineTemplates = new LineTemplate[ff.nLines];
			ff.lines = new Line[ff.nLines];
			
			int index = 0;
			
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
				
				Map<String, Object> drawDef = (Map<String, Object>) lineTemplateDef.get("draw");
				
				LineDraw draw = null;
				
				if (drawDef != null) {
					String drawName = (String) drawDef.get("name");
					
					if (drawName != null) {
						if (drawName.equals("SolidDraw")) {
							int red = getInt(drawDef, "red", 0);
							int green = getInt(drawDef, "green", 0);
							int blue = getInt(drawDef, "blue", 0);
							int strokeWidth = getInt(drawDef, "strokeWidth", 0);
							draw = new SolidDraw(red, green, blue, strokeWidth);
						} else if (drawName.equals("ImageDraw")) {
							int strokeWidth = getInt(drawDef, "strokeWidth", 0);
							String image = (String) drawDef.get("image");
							ImageGrid imageGrid = ff.imageGridMap.get(image);
							draw = new ImageDraw(imageGrid, strokeWidth);
						}
					}
				}
				
				if (draw == null) {
					draw = new SolidDraw(0, 0, 0, 1);
				}
				
				// Parse start area
				
				Map<String, Object> startAreaDef = (Map<String, Object>) lineTemplateDef.get("startArea");
				
				StartArea startArea = null;
				
				if (startAreaDef != null) {
					String startAreaName = (String) startAreaDef.get("name");
					
					if (startAreaName != null) {
						if (startAreaName.equals("RectangleArea")) {
							int x = getInt(startAreaDef, "x", 0);
							int y = getInt(startAreaDef, "y", 0);
							int width = getInt(startAreaDef, "width", ff.width);
							int height = getInt(startAreaDef, "height", ff.height);
							startArea = new RectangleArea(ff, x, y, width, height);
							
//							drawBox(x, y, width, height);						
						}
					}
				}
				
				if (startArea == null) {
					startArea = masterStartArea;
				}
				
				// Parse threshold images
				
				ImageGrid thresholdImage = null;
				
				if (lineTemplateDef.containsKey("threshold")) {
					String image = (String) lineTemplateDef.get("threshold");
					thresholdImage = ff.imageGridMap.get(image);
				}
				
				ff.lineTemplates[index++] = new LineTemplate(straightProb, stepSpeed, drawSpeed, movement, direction, draw, startArea, thresholdImage);
			}
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
		
		public double getDouble(Map<String, Object> map, String key) {
			Double d = (Double) map.get(key);
			return d.doubleValue();
		}
		
		public double getDouble(Map<String, Object> map, String key, double def) {
			if (map.containsKey(key)) {
				return getDouble(map, key);
			} else {
				return def;
			}
		}
	}
	
	int				width				= 0;
	int				height				= 0;
	
	public int				widthSpacing		= 0;
	public int				heightSpacing		= 0;
	
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
	
	public void loadSettings(Settings s) {
		try {
			ConfigParser cp = new ConfigParser(new File("../configs/" + s.configFile), this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setup() {
		frameRate(30);
		
		loadSettings(DEFAULT_SETTING);
		
		size(width, height);
		background(255);
		
		// Create a list of coordinates that have not yet been visited
		
		grid = new boolean[rows()][columns()];
		
		// Create a list of directions that can be shuffled when lines are traversing
		
		dlist.clear();
		
		for (int i = 0; i < 8; i++) {
			dlist.add(i);
		}
		
		Collections.shuffle(dlist);
		
		// Create the lines
		
		for (int i = 0; i < nLines; i++) {
			lines[i] = newLine(lineTemplates[i]);
		}
		
		// Block off areas that are black according to a threshold pic
		
//		if (thresholdImage != null) {
//			if (DEBUG) {
//				File f = new File(thresholdImage);
//				System.err.println("threshold image path: " + f.getAbsolutePath());
//			}
//			
//			thresholdPic = loadImage(thresholdImage);
//			size(thresholdPic.width, thresholdPic.height);
//			filter(BLUR);
//			filter(THRESHOLD);
//			
//			thresholdPic.loadPixels();
//			
//			for (int r = 0; r < rows(); r++) {
//				for (int c = 0; c < columns(); c++) {
//					int count = 0;
//					int black = 0;
//					
//					for (int x = (int) Math.round(widthSpacing * c); x < Math.round(widthSpacing * (c+1)); x++) {
//						for (int y = (int) Math.round(heightSpacing * r); y < Math.round(heightSpacing * (r+1)); y++) {
//							count++;
//							
//							// Take a CMY version of colour. If a colour is perceptibly non-white then treat it as an area to fill in.
//							
//							int colour = thresholdPic.pixels[y * thresholdPic.width + x];
//							float thresholdValue = (255 - red(colour)) + (255 - green(colour)) + (255 - blue(colour));
//							
//							black += thresholdValue > MIN_THRESHOLD_VALUE ? 1 : 0;
//						}
//					}
//					
//					grid[r][c] |= black < count / 2;
//
//					if (DEBUG && grid[r][c]) {
//						fill(255, 0, 0, 128);
//						noStroke();
//						rect(c * widthSpacing, r * heightSpacing, widthSpacing, heightSpacing);
//					}
//				}
//			}
//		}
	}
	
	boolean isDone = false;
	
	public void draw() {
		if (keyPressed) {
			if (key == ' ') {
				pause = !pause;
			}
		}
		
		if (!pause) {
			stroke(0);
			
			boolean done = true;
			
			for (int i = 0; i < nLines; i++) {
				if (lines[i] != null) {
					if (!lines[i].forwardDraw()) {
						lines[i] = newLine(lineTemplates[i]);
					}
					
					done = false;
				}
			}
			
			if (done) {
				if (!isDone) {
					System.out.println("done");
					isDone = true;
				}
			}
		}
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
	
	public Line newLine(LineTemplate lineTemplate) {
		int gr = 0;
		int gc = 0;
		int gd = -1;
		
		// This loops will break when there are no more places to try and place lines.
		
		while (gd == -1) {
			StartArea startArea = lineTemplate.startArea; 
			
			// If there is no start point, we cannot create this line.
			
			if (!startArea.getNextStartPoint()) {
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
	
	int red[] = new int[] { 200, 215, 215 };
	int blue[] = new int[] { 200, 111, 0 };
	int green[] = new int[] { 200, 0, 0 };

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
	
	public int rows() {
		return height / heightSpacing;
	}
	
	public int columns() {
		return width / widthSpacing;
	}
}
