package dev.boxy.fortyfive;
import java.io.*;
import java.util.*;

import processing.core.*;

public class ImageGrid {
	
	public static final int			MIN_THRESHOLD_VALUE		= 50;

	FortyFive	ff;
	
	protected String		name;
	
	protected String		colourImage 		= null;
	protected PImage		colourPic;
	
	/**
	 * Store the threshold versions of this image per scale
	 */
	Map<Record, boolean[][]> thresholdCache = new HashMap<Record, boolean[][]>();
	
	/**
	 * Store the colour grid versions of this image per scale
	 */
	Map<Record, int[][]> colourGridCache = new HashMap<Record, int[][]>();
	
	public ImageGrid(FortyFive ff, String name, String colourImage) {
		this.ff = ff;
		this.name = name;
		this.colourImage = colourImage;
		
		TimingUtils.markAdd("load image");
		colourPic = ff.loadImage(colourImage);
		TimingUtils.markAdd("load image");

		if (colourPic == null) {
			throw new RuntimeException("File not found: " + colourImage + " " + new File(colourImage).getAbsolutePath());
		}
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
		TimingUtils.markAdd("precompute colour grid");
		
		colourPic = ff.loadImage(colourImage);
		
		int origWidth = colourPic.width;
		int origHeight = colourPic.height;
		
		colourPic.resize((int) (origWidth * scale), (int) (origHeight * scale));
		
		colourPic.loadPixels();
		
		int width = (int) colourPic.width;
		int height = (int) colourPic.height;
		
		int rows = ff.rows(height);
		int columns = ff.columns(width);
		
		int[][] colourGrid = new int[rows][columns];
		
		int widthSpacing = ff.widthSpacing;
		int heightSpacing = ff.heightSpacing;
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				float red = 0;
				float g = 0;
				float b = 0;
				int count = 0;
				
				for (int x = (int) (c * widthSpacing); x < ((c+1) * widthSpacing); x++) {
					for (int y = (int) (r * heightSpacing); y < ((r+1) * heightSpacing); y++) {
						int pixel = colourPic.pixels[y * width + x];
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
		
		TimingUtils.markAdd("precompute colour grid");

		return colourGrid;
	}
	
	public boolean[][] precomputeThreshold(double scale/*, boolean invert*/) {
		PImage thresholdPic = ff.loadImage(colourImage);
		
		int width = (int) (thresholdPic.width * scale);
		int height = (int) (thresholdPic.height * scale);
		
		int rows = ff.rows(height);
		int columns = ff.columns(width);
		
		thresholdPic.resize(width, height);
		thresholdPic.filter(ff.BLUR);
		thresholdPic.filter(ff.THRESHOLD);
		
		boolean[][] threshold = new boolean[rows][columns];
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				int count = 0;
				int black = 0;
				
				for (int x = (int) Math.round(ff.widthSpacing * c); x < Math.round(ff.widthSpacing * (c+1)); x++) {
					for (int y = (int) Math.round(ff.heightSpacing * r); y < Math.round(ff.heightSpacing * (r+1)); y++) {
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
			int tr = ff.yToRow(ff.rowToY(r) + yOffset);
			
			if (tr < 0) {
				continue;
			} else if (tr >= brows) {
				break;
			}
			
			for (int c = 0; c < tcolumns; c++) {
				int tc = ff.xToColumn(ff.columnToX(c) + xOffset);
				
				if (tc < 0) {
					continue;
				} else if (tc >= bcolumns) {
					break;
				}
				
				blocked[tr][tc] |= (threshold[r][c] ^ invert);
				
				if (FortyFive.SHOW_THRESHOLD && blocked[tr][tc]) {
					ff.fill(255, 0, 0, 128);
					ff.noStroke();
					ff.rect(tc * ff.widthSpacing, tr * ff.heightSpacing, ff.widthSpacing, ff.heightSpacing);
				}
			}
		}
	}
	
	public int getWidth() {
		return colourPic.width;
	}
	
	public int getHeight() {
		return colourPic.height;
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
