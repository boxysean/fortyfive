package dev.boxy.fortyfive;

import processing.core.*;
import dev.boxy.fortyfive.core.scene.*;

public class GridLayer implements FortyFiveLayer {

	protected SceneGeometry geometry;
	
	protected int xOffset;
	protected int yOffset;
	protected PGraphics g;
	
	protected int red;
	protected int green;
	protected int blue;
	
	protected String name;
	
	public GridLayer(SceneGeometry geometry, boolean[][] grid) {
		this(geometry, grid, 0, 0);
	}
	
	public GridLayer(SceneGeometry geometry, boolean[][] grid, int xOffset, int yOffset) {
		this(geometry, grid, xOffset, yOffset, 255, 0, 0);
	}
	
	public GridLayer(SceneGeometry geometry, boolean[][] grid, int xOffset, int yOffset, int red, int green, int blue) {
		this.geometry = geometry;
		
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		
		this.red = red;
		this.green = green;
		this.blue = blue;
		
		update(grid);
	}
	
	public void update(boolean[][] grid) {
		FortyFive ff = FortyFive.getInstance();
		
		if (g == null) {
			g = ff.createGraphics(ff.width, ff.height, PApplet.P2D);
		} else {
			g.beginDraw();
			g.background(0);
			g.endDraw();
		}
		
		int trows = grid.length;
		int tcolumns = grid[0].length;
		
		int brows = geometry.rows();
		int bcolumns = geometry.columns();

		g.beginDraw();
		
		for (int r = 0; r < trows; r++) {
			int tr = geometry.yToRow(geometry.rowToY(r) + yOffset);
			
			if (tr < 0) {
				continue;
			} else if (tr >= brows) {
				break;
			}
			
			for (int c = 0; c < tcolumns; c++) {
				int tc = geometry.xToColumn(geometry.columnToX(c) + xOffset);
				
				if (tc < 0) {
					continue;
				} else if (tc >= bcolumns) {
					break;
				}
				
				if (grid[r][c]) {
					g.fill(red, green, blue);
					g.noStroke();
					g.rect(tc * geometry.getWidthSpacing(), tr * geometry.getHeightSpacing(), geometry.getWidthSpacing(), geometry.getHeightSpacing());
				}
			}
		}
		
		g.endDraw();
	}
	
	public void draw(PGraphics pg) {
//		pg.alpha(128);
		pg.image(g.get(), 0, 0);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getOrder() {
		return 10;
	}
	
	public String toString() {
		return name;
	}

}
