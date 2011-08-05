package dev.boxy.fortyfive.core.image;
import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;

import processing.core.*;

public class ImageGrid {
	
	public static final int			MIN_THRESHOLD_VALUE		= 50;

	protected FortyFive ff;
	protected SceneFactory sceneFactory;
	protected String name;
	protected String imageFile;
	protected PImage image;
	
	/**
	 * Store the threshold versions of this image per scale
	 */
	protected Map<Record, boolean[][]> thresholdCache = new HashMap<Record, boolean[][]>();
	
	/**
	 * Store the colour grid versions of this image per scale
	 */
	protected Map<Record, int[][]> colourGridCache = new HashMap<Record, int[][]>();
	
	public ImageGrid(SceneFactory sceneFactory, String name, String imageFile, PImage image) {
		this.sceneFactory = sceneFactory;
		this.ff = FortyFive.getInstance();
		this.name = name;
		this.imageFile = imageFile;
		this.image = image;
	}
	
	/**
	 * Returns the colour of the image grid at the particular row and column coordinate
	 * @param r
	 * @param c
	 * @return
	 */
	public int colourAt(int r, int c, double scale) {
		int[][] colourGrid = getColourGrid(scale);
		
		if (0 <= r && r < colourGrid.length && 0 <= c && c < colourGrid[0].length) {
			return colourGrid[r][c];
		} else {
			return ff.color(0);
		}
	}
	
	public int[][] precomputeColourGrid(double scale) {
		PImage image = ff.loadImage(imageFile);

		int origWidth = image.width;
		int origHeight = image.height;
		
		image.resize((int) (origWidth * scale), (int) (origHeight * scale));
		
		image.loadPixels();
		
		int width = (int) image.width;
		int height = (int) image.height;
		
		int rows = sceneFactory.rows(height);
		int columns = sceneFactory.columns(width);
		
		int[][] colourGrid = new int[rows][columns];
		
		int widthSpacing = sceneFactory.getWidthSpacing();
		int heightSpacing = sceneFactory.getHeightSpacing();
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				float red = 0;
				float g = 0;
				float b = 0;
				int count = 0;
				
				for (int x = (int) (c * widthSpacing); x < ((c+1) * widthSpacing); x++) {
					for (int y = (int) (r * heightSpacing); y < ((r+1) * heightSpacing); y++) {
						int pixel = image.pixels[y * width + x];
						red += ff.red(pixel);
						g += ff.green(pixel);
						b += ff.blue(pixel);
						count++;
					}
				}
				
				colourGrid[r][c] = ff.color(red / count, g / count, b / count);
				
//				ff.drawBox(r, c, colourGrid[r][c]);
			}
		}

		return colourGrid;
	}
	
	public boolean[][] precomputeThreshold(double scale/*, boolean invert*/) {
		PImage thresholdPic = ff.loadImage(imageFile);
		
		int width = (int) (thresholdPic.width * scale);
		int height = (int) (thresholdPic.height * scale);
		
		int rows = sceneFactory.rows(height);
		int columns = sceneFactory.columns(width);
		
		thresholdPic.resize(width, height);
		thresholdPic.filter(PApplet.BLUR);
		thresholdPic.filter(PApplet.THRESHOLD);
		
		boolean[][] threshold = new boolean[rows][columns];
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				int count = 0;
				int black = 0;
				
				for (int x = (int) Math.round(sceneFactory.getWidthSpacing() * c); x < Math.round(sceneFactory.getWidthSpacing() * (c+1)); x++) {
					for (int y = (int) Math.round(sceneFactory.getHeightSpacing() * r); y < Math.round(sceneFactory.getHeightSpacing() * (r+1)); y++) {
						count++;
						
						// Take a CMY version of colour. If a colour is perceptibly non-white then treat it as an area to fill in.
						
						int colour = thresholdPic.pixels[y * thresholdPic.width + x];
						float thresholdValue = (255 - ff.red(colour)) + (255 - ff.green(colour)) + (255 - ff.blue(colour));
						
						black += thresholdValue > MIN_THRESHOLD_VALUE ? 1 : 0;
					}
				}
				
				threshold[r][c] |= black < count / 2;
			}
		}
		
		return threshold;
	}

	/**
	 * Finds or computes threshold boolean map for the image at the particular scale.
	 * @param scale the scale of the image
	 * @return threshold boolean map
	 */
	public boolean[][] getThreshold(double scale) {
		Record r = new Record(scale);
		
		boolean[][] threshold = thresholdCache.get(r);

		if (threshold == null) {
			threshold = precomputeThreshold(scale);
			thresholdCache.put(r, threshold);
		}
		
		return threshold;
	}
	
	/**
	 * Finds or computes colour grid for the image at the particular scale.
	 * @param scale the scale of the image
	 * @return colour grid map
	 */
	public int[][] getColourGrid(double scale) {
		Record r = new Record(scale);
		
		int[][] colourGrid = colourGridCache.get(r);

		if (colourGrid == null) {
			colourGrid = precomputeColourGrid(scale);
			colourGridCache.put(r, colourGrid);
		}
		
		return colourGrid;
	}
	
	public void applyThreshold(boolean[][] blocked, boolean invert, int xOffset, int yOffset, double scale) {
		boolean[][] threshold = getThreshold(scale);
		
		int trows = threshold.length;
		int tcolumns = threshold[0].length;
		
		int brows = blocked.length;
		int bcolumns = blocked[0].length;
		
		for (int r = 0; r < trows; r++) {
			int tr = sceneFactory.yToRow(sceneFactory.rowToY(r) + yOffset);
			
			if (tr < 0) {
				continue;
			} else if (tr >= brows) {
				break;
			}
			
			for (int c = 0; c < tcolumns; c++) {
				int tc = sceneFactory.xToColumn(sceneFactory.columnToX(c) + xOffset);
				
				if (tc < 0) {
					continue;
				} else if (tc >= bcolumns) {
					break;
				}
				
				blocked[tr][tc] |= (threshold[r][c] ^ invert);
				
				if (FortyFive.SHOW_THRESHOLD && blocked[tr][tc]) {
					ff.fill(255, 0, 0, 128);
					ff.noStroke();
					ff.rect(tc * sceneFactory.getWidthSpacing(), tr * sceneFactory.getHeightSpacing(), sceneFactory.getWidthSpacing(), sceneFactory.getHeightSpacing());
				}
			}
		}
	}
	
	public int getWidth() {
		return image.width;
	}
	
	public int getHeight() {
		return image.height;
	}
	
	public String getName() {
		return name;
	}
	
	class Record {
		
		double scale;
		
		public Record(double scale) {
			this.scale = scale;
		}
		
		@Override
		public int hashCode() {
			// TODO this is probably not a good idea because doubles always change
			return new Double(scale).hashCode() * 2;
		}
		
		@Override
		public boolean equals(Object o) {
			try {
				Record r = (Record) o;
				
				if (Math.abs(r.scale - scale) > 1e-7) {
					return false;
				}
				
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		
	}
	
}
