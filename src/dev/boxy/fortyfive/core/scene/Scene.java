package dev.boxy.fortyfive.core.scene;

import java.util.*;

import processing.core.*;
import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.areas.*;
import dev.boxy.fortyfive.core.colour.*;
import dev.boxy.fortyfive.core.draw.*;
import dev.boxy.fortyfive.core.image.*;
import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.movement.*;
import dev.boxy.fortyfive.core.presentation.*;
import dev.boxy.fortyfive.core.startarea.*;
import dev.boxy.fortyfive.utils.*;

public class Scene extends SceneGeometry implements FortyFiveLayer {
	
	// 0 = top, 1 = top right, ..., 7 = top left
	public static final int[]	dr		= new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	public static final int[]	dc		= new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };

	protected SceneFactory sceneFactory;
	
	protected int drawSpeedMultiplier;
	protected List<String> lineNames = new ArrayList<String>();

	protected Map<String, Colour> colours = new LinkedHashMap<String, Colour>();
	protected Map<String, ColourPalette> colourPalettes = new LinkedHashMap<String, ColourPalette>();
	protected Map<String, LineDraw> lineDraws = new LinkedHashMap<String, LineDraw>();
	
	// TODO none of these lists get populated at all!~~~~~~~~~~~~
	
	protected int nLines;
	
	protected Line[] lines;
	
	protected int[] speedRem;
	
	protected boolean[][] grid;
	
	protected List<Integer> dlist = new LinkedList<Integer>();
	
	protected PGraphics pg;
	
	public Scene(SceneFactory sceneFactory, List<ColourFactory> colourFactories, List<ColourPaletteFactory> colourPaletteFactories,
			List<LineDrawFactory> lineDrawFactories, List<StartAreaFactory> startAreaFactories, List<String> lineNames, 
			String bgColour, int widthSpacing, int heightSpacing, int frameRate, int drawSpeedMultiplier) {
		super(widthSpacing, heightSpacing);
		
		this.sceneFactory = sceneFactory;
		
		for (ColourFactory colourFactory : colourFactories) {
			Colour colour = colourFactory.get();
			this.colours.put(colour.getName(), colour);
		}
		
		for (ColourPaletteFactory colourPaletteFactory : colourPaletteFactories) {
			ColourPalette colourPalette = colourPaletteFactory.get(this);
			this.colourPalettes.put(colourPalette.getName(), colourPalette);
		}
		
		for (LineDrawFactory lineDrawFactory : lineDrawFactories) {
			LineDraw lineDraw = lineDrawFactory.get(this);
			this.lineDraws.put(lineDraw.getName(), lineDraw);
		}
		
		this.lineNames.addAll(lineNames);
		
		this.nLines = lineNames.size();
		this.lines = new Line[nLines];
		this.speedRem = new int[nLines];
		
		this.grid = new boolean[ff.getHeight()][ff.getWidth()];
		this.drawSpeedMultiplier = 1;
		
		this.widthSpacing = widthSpacing;
		this.heightSpacing = heightSpacing;
		
		this.pg = ff.createGraphics(ff.width, ff.height, PApplet.P2D);
		
		if (bgColour.equalsIgnoreCase("black")) {
			pg.beginDraw();
			pg.background(0);
			pg.color(0);
			pg.fill(0);
			pg.noStroke();
			pg.rect(0, 0, ff.width, ff.height);
			pg.endDraw();
		} else if (bgColour.equalsIgnoreCase("white")) {
			pg.beginDraw();
			pg.background(255);
			pg.color(255);
			pg.fill(255);
			pg.noStroke();
			pg.rect(0, 0, ff.width, ff.height);
			pg.endDraw();
		}
		
		// TODO move to template???
		
		ff.frameRate(frameRate);
	}
	
	public void setup() {
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
			String lineName = lineNames.get(i);
			LineFactory lineFactory = getLineFactory(lineName);
			
			if (lineFactory == null) {
				Logger logger = Logger.getInstance();
				logger.warning("Scene.setup(): no such line name %s", lineName);
			}
			
			lines[i] = newLine(lineFactory);
		}
	}
	
	public void draw(PGraphics g) {
		if (FortyFive.getInstance().isPaused()) {
			return;
		}
		
		boolean finished = true;
		
		/*
		 * DrawSpeed mode is (a) for each line and (b) for each draw speed
		 */
		
//		for (int i = 0; i < nLines; i++) {
//			Line line = lines[i];
//			
//			int multiplier = userDrawSpeedMultiplier;
//			
//			if (line != null) {
//				for (int j = 0; j < line.drawSpeed * multiplier; j++) {
//					if (!line.forwardDraw()) {
//						line = newLine(lineFactories[i]);
//						lines[i] = line;
//					}
//					
//					TimingUtils.markAdd("draw");
//					
//					if (line == null) {
//						break;
//					}
//					
//					finished = false;
//				}
//			}
//		}
		
		/*
		 * DrawSpeed mode is (a) for each draw speed and (b) for each line
		 */
		
		if (speedRem == null || speedRem.length != nLines) {
			speedRem = new int[nLines];
		}
		
		for (int i = 0; i < nLines; i++) {
			if (lines[i] != null) {
				speedRem[i] = lines[i].getSpeed() * ff.getUserDrawSpeedMultiplier();
			}
		}
		
		boolean complete = false;
		
		while (!complete) {
			complete = true;
			
			for (int i = 0; i < nLines; i++) {
				Line line = lines[i];
				
				if (line != null && speedRem[i] > 0) {
					if (!line.forwardDraw()) {
						line = newLine(getLineFactory(lineNames.get(i)));
						lines[i] = line;
					}
					
					speedRem[i]--;
					complete = false;
					finished = false;
				}
			}
		}
		
		g.image(pg.get(), 0, 0, ff.width, ff.height);
		
		Presentation presentation = ff.getPresentation();
		
		if (finished) {
			presentation.onFinished();
			FortyFive.ITERATIONS++;
		} else {
			presentation.nextFrame();
		}
	}
	
	public void drawLine(int gr, int gc, int grr, int gcc, LineDraw draw) {
		float px = gridToPixel(gc, columns(), ff.getWidth());
		float py = gridToPixel(gr, rows(), ff.getHeight());
		float pxx = gridToPixel(gcc, columns(), ff.getWidth());
		float pyy = gridToPixel(grr, rows(), ff.getHeight());
		
		draw.drawLine(pg, gr, gc, grr, gcc, px, py, pxx, pyy);
	}
	
	public void drawBox(int gr, int gc) {
		drawBox(gr, gc, ff.color(255, 225, 225));
	}
	
	public void drawBox(int gr, int gc, int color) {
		float px = gridToPixel(gc, columns(), ff.getWidth()) - (widthSpacing / 2);
		float py = gridToPixel(gr, rows(), ff.getHeight()) - (heightSpacing / 2);
		
		ff.noStroke();
		ff.fill(color);
		ff.rect(px, py, widthSpacing, heightSpacing);
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
		
		ff.noStroke();
		ff.fill(ff.color(255, 225, 225));
		ff.rect(px, py, pxx, pyy);
	}

	public Line newLine(LineFactory lineFactory) {
		int gr = 0;
		int gc = 0;
		int gd = -1;
		
		// This loops will break when there are no more places to try and place lines.
		
		while (gd == -1) {
			StartArea startArea = lineFactory.getStartArea();
			
			// If there is no start point, we cannot create this line.
			
			if (!startArea.getNextStartPoint(this)) {
				return null;
			}
			
			gc = startArea.getStartColumn();
			gr = startArea.getStartRow();
			
			// drawBox(gr, gc);
			
			// Pick a random direction; the first highest directional value that does not make an illegal move. 
			
			int highestValue = -1;
			int highestDirection = -1;
			
			for (int d : dlist) {
				if (lineFactory.getDirection(d) > highestValue) {
					int nr = gr + dr[d];
					int nc = gc + dc[d];
					
					if (!invalidMove(gr, gc, nr, nc)) {
						highestValue = lineFactory.getDirection(d);
						highestDirection = d;
					}
				}
			}
			
			gd = highestDirection;
			
			Collections.shuffle(dlist);
		}
		
		return lineFactory.get(this, gr, gc, gd);
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
		
		// check if crossing a diagonal move
		
		invalid |= cr != nr && cc != nc && (grid[nr][cc] || (blocked != null && blocked[nr][cc])) && (grid[cr][nc] || (blocked != null && blocked[cr][nc]));
		
		return invalid;
	}

	public List<Area> getAreas() {
		return sceneFactory.getAreas();
	}
	
	public Area getArea(String name) {
		return sceneFactory.getArea(name);
	}
	
	public ImageGrid getImageGrid(String name) {
		return sceneFactory.getImageGrid(name);
	}
	
	public LineMovementFactory getLineMovementFactory(String name) {
		return sceneFactory.getLineMovementFactory(name);
	}
	
	public LineDraw getLineDraw(String name) {
		LineDraw res = lineDraws.get(name);
		
		if (res == null) {
			Logger.getInstance().warning("no such line draw %s", name);
		}
		
		return res;
	}
	
	public ColourPalette getColourPalette(String name) {
		ColourPalette res = colourPalettes.get(name);
		
		if (res == null) {
			Logger.getInstance().warning("no such colour palette %s", name);
		}
		
		return res;
	}
	
	public Colour getColour(String name) {
		Colour res = colours.get(name);
		
		if (res == null) {
			Logger.getInstance().warning("no such colour %s", name);
		}
		
		return res;
	}
	
	public LineFactory getLineFactory(String name) {
		return sceneFactory.getLineFactory(name);
	}
	
	public boolean checkGrid(int r, int c) {
		return grid[r][c];
	}
	
	public void markGrid(int r, int c) {
		grid[r][c] = true;
	}
	
	public int getOrder() {
		return 1 << 31;
	}
	
}
