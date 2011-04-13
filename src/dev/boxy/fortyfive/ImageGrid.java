package dev.boxy.fortyfive;
import java.io.*;

import processing.core.*;

public class ImageGrid {
	
	FortyFive	ff;
	
	String		colourImage 		= null;
	PImage		colourPic;
	int[][]		colourGrid;
	
	boolean[][]	threshold;
	
	public ImageGrid(FortyFive ff, String colourImage) {
		this.ff = ff;
		this.colourImage = colourImage;
		
		populateColourGrid(colourImage);
	}
	
	public int at(int r, int c) {
		return colourGrid[r][c];
	}
	
	public void populateColourGrid(String colourImage) {
		colourPic = ff.loadImage(colourImage);
		
		if (colourPic == null) {
			throw new RuntimeException("File not found: " + colourImage + " " + new File(colourImage).getAbsolutePath());
		}
		
		colourPic.loadPixels();
		
		colourGrid = new int[ff.rows()][ff.columns()];
		
		int width = colourPic.width;
		int height = colourPic.height;
		
		int widthSpacing = ff.widthSpacing;
		int heightSpacing = ff.heightSpacing;
		
		for (int r = 0; r < ff.rows(); r++) {
			for (int c = 0; c < ff.columns(); c++) {
				float red = 0;
				float g = 0;
				float b = 0;
				int count = 0;
				
				for (int x = (int) Math.round(c * widthSpacing); x < Math.round((c+1) * widthSpacing); x++) {
					for (int y = (int) Math.round(r * heightSpacing); y < Math.round((r+1) * heightSpacing); y++) {
						int pixel = colourPic.pixels[y * width + x];
						red += ff.red(pixel);
						g += ff.green(pixel);
						b += ff.blue(pixel);
						count++;
					}
				}
				
				colourGrid[r][c] = ff.color(red / count, g / count, b / count);
				
//				drawBox(r, c, colourGrid[r][c]);
			}
		}
	}
	
	public void precomputeThreshold() {
		threshold = new boolean[ff.rows()][ff.columns()];
		
		PImage thresholdPic = colourPic;
		
		ff.size(thresholdPic.width, thresholdPic.height);
		ff.filter(ff.BLUR);
		ff.filter(ff.THRESHOLD);
		
		thresholdPic.loadPixels();
		
		for (int r = 0; r < ff.rows(); r++) {
			for (int c = 0; c < ff.columns(); c++) {
				int count = 0;
				int black = 0;
				
				for (int x = (int) Math.round(ff.widthSpacing * c); x < Math.round(ff.widthSpacing * (c+1)); x++) {
					for (int y = (int) Math.round(ff.heightSpacing * r); y < Math.round(ff.heightSpacing * (r+1)); y++) {
						count++;
						
						// Take a CMY version of colour. If a colour is perceptibly non-white then treat it as an area to fill in.
						
						int colour = thresholdPic.pixels[y * thresholdPic.width + x];
						float thresholdValue = (255 - ff.red(colour)) + (255 - ff.green(colour)) + (255 - ff.blue(colour));
						
						black += thresholdValue > ff.MIN_THRESHOLD_VALUE ? 1 : 0;
					}
				}
				
				threshold[r][c] |= black < count / 2;

				if (ff.DEBUG && threshold[r][c]) {
					ff.fill(255, 0, 0, 128);
					ff.noStroke();
					ff.rect(c * ff.widthSpacing, r * ff.heightSpacing, ff.widthSpacing, ff.heightSpacing);
				}
			}
		}
	}
	
	public void applyThreshold(boolean[][] grid) {
		if (threshold == null) {
			precomputeThreshold();
		}
		
		int rows = Math.min(threshold.length, grid.length);
		int columns = Math.min(threshold[0].length, grid[0].length);
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				grid[r][c] |= threshold[r][c];
			}
		}
	}

}
