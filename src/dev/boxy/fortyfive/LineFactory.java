package dev.boxy.fortyfive;

import java.util.*;

import dev.boxy.fortyfive.coordinatebag.*;
import dev.boxy.fortyfive.draw.*;
import dev.boxy.fortyfive.movement.*;

public class LineFactory implements FortyFiveLoader {
	
	public final static double 		DEFAULT_STRAIGHT_PROB 	= 0.80;
	public final static int 		DEFAULT_STEP_SPEED 		= 1;
	public final static int 		DEFAULT_DRAW_SPEED 		= 1;
	public final static int[]		DEFAULT_DIRECTION		= new int[] { 2, 2, 2, 2, 2, 2, 2, 2 };
	
	public static final int			IMAGE_THRESHOLD_FUDGE_FACTOR = 7; //pixels
	
	protected double				straightProb;
	protected int					stepSpeed;
	protected int					drawSpeed;
	protected LineMovement			lineMovement;
	protected int[]					direction;
	protected LineDrawFactory		lineDrawFactory;
	protected StartArea				startArea;
	protected List<ImageThreshold>	thresholds;
	
	public LineFactory(Map<String, Object> map) {
		loadSettings(map);
	}
	
	public Line get(int br, int bc, int bd, FortyFive ff) {
		return new Line(br, bc, bd, ff, this);
	}
	
	public void loadSettings(Map<String, Object> map) {
		FortyFive ff = FortyFive.getInstance();
		Logger logger = Logger.getInstance();
		ImageGridMap imageGridMap = ImageGridMap.getInstance();
		
		straightProb = ConfigParser.getDouble(map, "straightProb", DEFAULT_STRAIGHT_PROB);
		stepSpeed = ConfigParser.getInt(map, "stepSpeed", DEFAULT_STEP_SPEED);
		drawSpeed = ConfigParser.getInt(map, "drawSpeed", DEFAULT_DRAW_SPEED);
		
		// Parse movement parameters
		
		Map<String, Object> movementDef = (Map<String, Object>) map.get("movement");
		
		lineMovement = null;
		
		if (movementDef != null) {
			String movementName = (String) movementDef.get("name");
			
			if (movementName != null) {
				if (movementName.equals("ClingMovement")) {
					lineMovement = new ClingMovement(ff, null);
				} else if (movementName.equals("IntelligentMovement")) {
					int movementIntelligence = ConfigParser.getInt(movementDef, "intelligence", 2);
					lineMovement = new IntelligentMovement(ff, null, movementIntelligence);
				}
			}
		}
		
		if (lineMovement == null) {
			lineMovement = new IntelligentMovement(ff, null, 2);
		}
		
		// Parse direction string
		
		String directionStr = (String) map.get("direction");
		direction = new int[8];
		Arrays.fill(direction, 2);
		
		if (directionStr != null) {
			for (int i = 0; i < Math.min(directionStr.length(), direction.length); i++) {
				direction[i] = (int) directionStr.charAt(i) - '0';
			}
		}
		
		// Parse draw method
		
		TimingUtils.markAdd("parse draw");
		
		Map<String, Object> drawDef = (Map<String, Object>) map.get("draw");
		
		if (drawDef != null) {
			String drawName = ConfigParser.getString(drawDef, "name", "SolidDraw");
			
			if (drawName.equals("SolidDraw")) {
				lineDrawFactory = new SolidDrawFactory(drawDef);
			} else if (drawName.equals("ImageDraw")) {
				lineDrawFactory = new ImageDrawFactory(drawDef);
			}
		}
		
		if (lineDrawFactory == null) {
			logger.warning("draw init: no draw defined");
			lineDrawFactory = LineDrawFactory.DEFAULT;
		}
		
		TimingUtils.markAdd("parse draw");

		// Parse threshold images
		
		TimingUtils.markAdd("threshold images");
		
		List<Map<String, Object>> thresholdDefs = (List<Map<String, Object>>) map.get("threshold");
		
		thresholds = new LinkedList<ImageThreshold>();
		
		if (thresholdDefs != null) {
			for (Map<String, Object> thresholdDef : thresholdDefs) {
				String thresholdName = (String) thresholdDef.get("name");
				boolean thresholdInvert = ConfigParser.getBoolean(thresholdDef, "invert", false);
				int thresholdXOffset = ConfigParser.getInt(thresholdDef, "xOffset", 0);
				int thresholdYOffset = ConfigParser.getInt(thresholdDef, "yOffset", 0);
				double thresholdScale = ConfigParser.getDouble(thresholdDef, "scale", 1.0);
				
				ImageGrid thresholdImage = imageGridMap.get(thresholdName);
				
				if (thresholdImage == null) {
					System.err.printf("threshold image warning: no such threshold name as %s\n", thresholdName);
				}
				
				if (thresholdScale < 0) {
					// Auto scale to size of screen
					int imageWidth = thresholdImage.colourPic.width;
					int imageHeight = thresholdImage.colourPic.height;
					
					double widthResizeRatio = (double)  ff.getWidth() / imageWidth;
					double heightResizeRatio = (double) ff.getHeight() / imageHeight;
					
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
					thresholdXOffset = (int) (ff.getWidth() - (thresholdImage.colourPic.width * thresholdScale)) / 2;
				}
				
				if (thresholdYOffset < 0) {
					// Center the image along x
					thresholdYOffset = (int) (ff.getHeight() - (thresholdImage.colourPic.height * thresholdScale)) / 2;
				}
				
				thresholds.add(new ImageThreshold(thresholdName, thresholdImage, thresholdInvert, thresholdXOffset, thresholdYOffset, thresholdScale));
			}
		}
		
		TimingUtils.markAdd("threshold images");
		
		// Parse start area
		
		TimingUtils.markAdd("start area");
		
		// First see if a coordinate bag is defined
		
		CoordinateBag coordBag = null;
		
		try {
			// Maybe the coord bag is a list of integers
			
			List<Integer> coordBagValues = (List<Integer>) map.get("coordBag");
			
			int leftFirst = coordBagValues.get(0);
			int topFirst = coordBagValues.get(1);
			
			coordBag = new OrderedBag(ff, leftFirst, topFirst);
		} catch (Exception e) {
			
		}
		
		try {
			// Maybe the coord bag is a string
			
			String coordBagName = ConfigParser.getString(map, "coordBag");
			
			if (coordBagName != null) {
				if (coordBagName.equalsIgnoreCase("ordered") || coordBagName.equalsIgnoreCase("forward")) {
					coordBag = new OrderedBag(ff, true);
				} else if (coordBagName.equalsIgnoreCase("backward")) {
					coordBag = new OrderedBag(ff, false);
				} else if (coordBagName.equalsIgnoreCase("random")) {
					coordBag = new RandomBag();
				} else if (coordBagName.equalsIgnoreCase("centre")) {
					coordBag = new CentreBag(ff);
				}
			}
		} catch (Exception e) {
			
		}
		
		if (coordBag == null) {
			coordBag = new RandomBag();
		}
		
		// Okay, now start initializing the start area
		
		List<Map<String, Object>> startAreaDefs = (List<Map<String, Object>>) map.get("startArea");
		
		if (startAreaDefs != null) {
			startArea = new StartArea(ff, coordBag);
			
			TimingUtils.markAdd("start area -- adding rects");
			
			for (Map<String, Object> startAreaDef : startAreaDefs) {
				if (startAreaDef.containsKey("addRect")) {
					List<Object> coords = (List<Object>) startAreaDef.get("addRect");
					
					int x = ConfigParser.parseInt(coords, 0, ff.width, 0);
					int y = ConfigParser.parseInt(coords, 1, ff.height, 0);
					int width = ConfigParser.parseInt(coords, 2, ff.width, ff.width);
					int height = ConfigParser.parseInt(coords, 3, ff.height, ff.height);
					
					startArea.addRectangle(x, y, width, height);
				}
				
				if (startAreaDef.containsKey("addImage")) {
					List<Object> image = (List<Object>) startAreaDef.get("addImage");
					
					// Allow some fudge factor in the config for extra flexibility. Positive offsets shrink the side.
					
					String imageName = (String) image.get(0);
					int topOffset = ConfigParser.getInt(image, 1, 0);
					int rightOffset = ConfigParser.getInt(image, 2, 0);
					int bottomOffset = ConfigParser.getInt(image, 3, 0);
					int leftOffset = ConfigParser.getInt(image, 4, 0);
					
					// TODO this implies that a start area cannot be made without defining it as a threshold, fail
					
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
					
					int x = ConfigParser.parseInt(coords, 0, ff.width, 0);
					int y = ConfigParser.parseInt(coords, 1, ff.height, 0);
					int width = ConfigParser.parseInt(coords, 2, ff.width, ff.width);
					int height = ConfigParser.parseInt(coords, 3, ff.height, ff.height);
					
					startArea.removeRectangle(x, y, width, height);
				}
				
				if (startAreaDef.containsKey("removeImage")) {
					List<Object> image = (List<Object>) startAreaDef.get("removeImage");
					
					String imageName = (String) image.get(0);
					int topOffset = ConfigParser.getInt(image, 1, 0);
					int rightOffset = ConfigParser.getInt(image, 2, 0);
					int bottomOffset = ConfigParser.getInt(image, 3, 0);
					int leftOffset = ConfigParser.getInt(image, 4, 0);
					
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
				
				if (startAreaDef.containsKey("debug")) {
					startArea.setDebug(true);
				}
			}
			
			TimingUtils.markAdd("start area -- adding rects");
		}
		
		TimingUtils.markAdd("start area -- master rect");

		if (startArea == null) {
			if (coordBag == null) {
				startArea = ff.masterStartArea;
			} else {
				startArea = new StartArea(ff, coordBag);
				startArea.addRectangle(0, 0, ff.getWidth(), ff.getHeight());
			}
		}
		
		TimingUtils.markAdd("start area -- master rect");
		
		TimingUtils.markAdd("start area -- commit");
		
		startArea.commitCoords(thresholds);
		
		TimingUtils.markAdd("start area -- commit");
		
		TimingUtils.markAdd("start area");
	}

}