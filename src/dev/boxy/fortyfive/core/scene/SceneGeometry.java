package dev.boxy.fortyfive.core.scene;

import dev.boxy.fortyfive.*;

public class SceneGeometry {
	
	protected FortyFive ff = FortyFive.getInstance();
	
	protected int heightSpacing;
	protected int widthSpacing;
	
	protected SceneGeometry(int widthSpacing, int heightSpacing) {
		this.widthSpacing = widthSpacing;
		this.heightSpacing = heightSpacing;
	}
	
	/**
	 * Give the number of rows in the main workspace
	 * @return rows in the main workspace
	 */
	public int rows() {
		return rows(ff.getHeight());
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
		return columns(ff.getWidth());
	}
	
	/**
	 * Give the number of columns in the workspace given the width
	 * @param width width of the workspace
	 * @return number of columns in the workspace given the width
	 */
	public int columns(int width) {
		return width / widthSpacing;
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
		return gridToPixel(c, columns(), ff.getWidth());
	}
	
	/**
	 * Given the x pixel space coordinate, return the grid column
	 * @param x x pixel space coordinate 
	 * @return grid column
	 */
	public int xToColumn(float x) {
		return pixelToGrid(x, ff.getWidth(), columns());
	}
	
	/**
	 * Given the grid row, return the y pixel space coordinate
	 * @param r grid row
	 * @return y pixel space coordinate
	 */
	public float rowToY(int r) {
		return gridToPixel(r, rows(), ff.getHeight());
	}
	
	/**
	 * Given the y pixel space coordinate, return the grid row
	 * @param y y pixel space coordinate
	 * @return grid row
	 */
	public int yToRow(float y) {
		return pixelToGrid(y, ff.getHeight(), rows());
	}
	
	public int getWidthSpacing() {
		return widthSpacing;
	}
	
	public int getHeightSpacing() {
		return heightSpacing;
	}
	
}
