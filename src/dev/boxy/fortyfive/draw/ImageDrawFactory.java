package dev.boxy.fortyfive.draw;

import java.util.*;

import dev.boxy.fortyfive.*;

public class ImageDrawFactory implements FortyFiveLoader, LineDrawFactory {

	protected ImageGrid imageGrid;
	protected int strokeWidth;
	protected String strokeJoinStr;
	protected String strokeCapStr;
	protected String image;
	protected int xOffset;
	protected int yOffset;
	protected double scale;
	protected int imageWidth;
	protected int imageHeight;
	
	public ImageDrawFactory(Map<String, Object> map) {
		loadSettings(map);
	}
	
	public void loadSettings(Map<String, Object> map) {
		FortyFive ff = FortyFive.getInstance();
		ImageGridMap imageGridMap = ImageGridMap.getInstance();
		
		strokeWidth = ConfigParser.getInt(map, "strokeWidth", 0);
		strokeJoinStr = ConfigParser.getString(map, "strokeJoin", "miter").toLowerCase();
		strokeCapStr = ConfigParser.getString(map, "strokeCap", "round").toLowerCase();
		image = ConfigParser.getString(map, "image");
		
		xOffset = ConfigParser.getInt(map, "xOffset", 0);
		yOffset = ConfigParser.getInt(map, "yOffset", 0);
		scale = ConfigParser.getDouble(map, "scale", 1.0);
		
		imageGrid = imageGridMap.get(image);
		
		if (scale < 0) {
			imageWidth = imageGrid.getWidth();
			imageHeight = imageGrid.getHeight();
			
			double widthResizeRatio = (double) ff.getWidth() / imageWidth;
			double heightResizeRatio = (double) ff.getHeight() / imageHeight;
			
			if (scale < -5) {
				scale = Math.max(widthResizeRatio, heightResizeRatio);
			} else {
				scale = Math.min(widthResizeRatio, heightResizeRatio);
			}
		}
		
		if (xOffset < 0) {
			// Center the image along x
			xOffset = (int) (ff.getWidth() - (imageGrid.getWidth() * scale)) / 2;
		}
		
		if (yOffset < 0) {
			// Center the image along y
			yOffset = (int) (ff.getHeight() - (imageGrid.getHeight() * scale)) / 2;
		}
	}
	
	public ImageDraw get() {
		return new ImageDraw(imageGrid, strokeWidth, xOffset, yOffset, scale, strokeJoinStr, strokeCapStr);
	}

}
