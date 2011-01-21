import java.util.*;

import processing.core.*;

public class FortyFive extends PApplet {
	
	public static final Settings	DEFAULT_SETTING		= Settings.DANSE_MID;
	
	public static final int		DIR_PREFERRED		= 2;
	public static final int		DIR_AVOID			= 1;
	public static final int		DIR_DISALLOWED		= 0;
	
	// 0 = top, 1 = top right, ..., 7 = top left
	public static final int[]	dr		= new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	public static final int[]	dc		= new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	
	public enum Settings {
		
		DANSE_MID (540, 720, 3, 3, 1, 5, 1, 2, null, "/Users/boxysean/Desktop/fortyfive/danse-mid.jpg", "22202220", null, null, 10),
		DANSE_BLACK (540, 720, 2, 2, 2, 10, 1, 2, null, "/Users/boxysean/Desktop/fortyfive/danse-black.jpg", "21212121", null, null, 10),
		DANSE_BLACK_DIAG (540, 720, 1, 1, 1, 10, 1, 0, null, "/Users/boxysean/Desktop/fortyfive/danse-black.jpg", "02020202", null, new int[] { 10, 10 }, 10),
		DANSE_WHITE (540, 720, 3, 3, 1, 5, 1, 2, null, "/Users/boxysean/Desktop/fortyfive/danse-white.jpg", null, null, null, 10),
		SUNSET (600, 399, 1, 1, 1, 10, 1, 0, null, "/Users/boxysean/Desktop/fortyfive/P1010475.JPG", "02000200", null, new int[] { 1, 1, 1, 2, 2, 2, 3, 3, 3, 4 }, 3),
		SMALL (200, 200, 20, 20, 1, 1, 1, 2, null, null, null, null, null, 1),
		EYE (180, 360, 2, 2, 1, 5, 1, 0, null, "/Users/boxysean/Desktop/fortyfive/eye.jpg", null, null, null, 5),
		VANCOUVER (480, 640, 2, 2, 0.98, 10, 2, 1, null, "/Users/boxysean/Desktop/fortyfive/vancouver.jpg", null, null, new int[] { 2, 2, 2, 2, 2, 2, 2, 2, 8, 8 }, 1),
		BRIDGE (800, 600, 4, 4, 0.9, 5, 2, 2, null, "/Users/boxysean/Desktop/fortyfive/20110114/bridge.jpg", null, null, null, 1),
		KETTLE (600, 800, 2, 2, 1, 5, 1, 0, null, "/Users/boxysean/Desktop/fortyfive/20110114/kettle.jpg", null, null, null, 1),
		RAVEN (600, 800, 6, 6, 0.75, 5, 2, 1, null, "/Users/boxysean/Desktop/fortyfive/20110114/raven.jpg", null, new int[] { 2, 2, 2, 2, 2 }, new int[] { 1, 2, 3, 4, 5 }, 1),
		HELICOPTER (600, 800, 10, 10, 0.75, 5, 2, 2, null, "/Users/boxysean/Desktop/fortyfive/20110114/helicopter.jpg", "20002000", null, null, 1),
		DALAI (800, 600, 2, 2, 0.75, 5, 1, 2, null, "/Users/boxysean/Desktop/fortyfive/20110114/dalai.jpg", null, null, null, 10);
		
		int		width;
		int		height;
		int		widthSpacing;
		int 	heightSpacing;
		double	straightProb;
		int 	nLines;
		int		strokeWidth;
		int		lineIntelligence;
		String	thresholdImage;
		String	colourImage;
		int[]	direction;
		int[]	stepSpeed;
		int[]	drawSpeed;
		
		/**
		 * 
		 * @param width canvas pixel width
		 * @param height canvas pixel height
		 * @param horSpacing pixels between horizontal lines
		 * @param verSpacing pixels between vertical lines
		 * @param straightProb probability on a step that 
		 * @param nLines number of lines drawing at a given time
		 * @param strokeWidth pixel width of the stroke
		 * @param lineIntelligence if 1 then an immediate dead-end will be avoided by turning; if 2 then a dead-end after two moves will be avoided; if 0 no dead-ends are avoided
		 * @param colourImage optional image to select drawn colours from
		 * @param directionString eight-character string, each character a number indicating directional preference starting from N to NW to ... to E to NE
		 * @param stepSpeedString nLines space-separated list, the ith number indicating the number of steps the ith line will take before drawing two points
		 * @param drawSpeedString nLines space-separated list, the ith number indicating the number of draws the ith line will take per frame
		 * @param drawSpeedMultiplier multiplier for every value in the drawSpeedString; speeds up the entire drawing programme
		 */
		private Settings(int width, int height, int widthSpacing, int heightSpacing, double straightProb, int nLines, int strokeWidth,
				int lineIntelligence, String thresholdImage, String colourImage, String directionString, int[] stepSpeed, int[] drawSpeed, double drawSpeedMultiplier) {
			this.width = width;
			this.height = height;
			this.widthSpacing = widthSpacing;
			this.heightSpacing = heightSpacing;
			this.straightProb = straightProb;
			this.nLines = nLines;
			this.strokeWidth = strokeWidth;
			this.lineIntelligence = lineIntelligence;
			this.thresholdImage = thresholdImage;
			this.colourImage = colourImage;
			
			if (directionString != null) {
				this.direction = new int[8];
				for (int i = 0; i < Math.min(8, directionString.length()); i++) {
					direction[i] = directionString.charAt(i) - '0';
				}
			}
			
			this.stepSpeed = new int[nLines];
			Arrays.fill(this.stepSpeed, 1);
			
			if (stepSpeed != null) {
				for (int i = 0; i < Math.min(nLines, stepSpeed.length); i++) {
					this.stepSpeed[i] = stepSpeed[i];
				}
			}
			
			this.drawSpeed = new int[nLines];
			Arrays.fill(this.drawSpeed, (int) Math.round(drawSpeedMultiplier));
			
			if (drawSpeed != null) {
				for (int i = 0; i < Math.min(nLines, drawSpeed.length); i++) {
					this.drawSpeed[i] = drawSpeed[i] * (int) Math.round(drawSpeedMultiplier);
				}
			}
		}
	}
	
	int		widthSpacing		= 0;
	int		heightSpacing		= 0;
	double	straightProb 		= 0.80;
	int		nLines 				= 5;
	int		strokeWidth 		= 1;
	int		lineIntelligence 	= 2;
	String	thresholdImage 		= null;
	String	colourImage 		= null;
	int[]	direction 			= new int[] { 2, 2, 2, 2, 2, 2, 2, 2 };
	int[]	stepSpeed			= null;
	int[]	drawSpeed			= null;
	
	boolean[][]		grid;
	int[][]			colourGrid;
	
	LinkedList<Integer>		rrem	= new LinkedList<Integer>();
	LinkedList<Integer>		crem	= new LinkedList<Integer>();
	List<Integer>			dlist	= new LinkedList<Integer>();
	
	PImage	thresholdPic;
	PImage	colourPic;
	
	Line[]	lines	= new Line[nLines];
	
	boolean	pause	= false;
	
	public void loadSettings(Settings s) {
		size(s.width, s.height);
		widthSpacing = s.widthSpacing;
		heightSpacing = s.heightSpacing;
		straightProb = s.straightProb;
		nLines = s.nLines;
		strokeWidth = s.strokeWidth;
		lineIntelligence = s.lineIntelligence;
		thresholdImage = s.thresholdImage;
		colourImage = s.colourImage;
		lines = new Line[nLines];
		
		if (s.direction != null) {
			direction = s.direction;
		}
		
		if (s.stepSpeed != null) {
			stepSpeed = s.stepSpeed;
		} else {
			stepSpeed = new int[nLines];
			Arrays.fill(stepSpeed, 1);
		}
		
		if (s.drawSpeed != null) {
			drawSpeed = s.drawSpeed;
		} else {
			drawSpeed = new int[nLines];
			Arrays.fill(drawSpeed, 1);
		}
	}
	
	public void setup() {
		frameRate(30);
		
		loadSettings(DEFAULT_SETTING);
		
		background(255);
		
		// Create a list of coordinates that have not yet been visited
		
		grid = new boolean[rows()][columns()];
		
		for (int r = 0; r < rows(); r++) {
			for (int c = 0; c < columns(); c++) {
				rrem.add(r);
				crem.add(c);
			}
		}
		
		// Create a list of directions that can be shuffled when lines are traversing
		
		dlist.clear();
		
		for (int i = 0; i < 8; i++) {
			dlist.add(i);
		}
		
		Collections.shuffle(dlist);
		
		// Create the lines
		
		for (int i = 0; i < nLines; i++) {
			lines[i] = newLine(stepSpeed[i], drawSpeed[i]);
		}
		
		// Block off areas that are black according to a threshold pic
		
		if (thresholdImage != null) {
			thresholdPic = loadImage(thresholdImage);
			size(thresholdPic.width, thresholdPic.height);
			filter(BLUR);
			filter(THRESHOLD);
			
			thresholdPic.loadPixels();
			
			for (int r = 0; r < rows(); r++) {
				for (int c = 0; c < columns(); c++) {
					int count = 0;
					int black = 0;
					for (int x = (int) Math.round(widthSpacing * c); x < Math.round(widthSpacing * (c+1)); x++) {
						for (int y = (int) Math.round(heightSpacing * r); y < Math.round(heightSpacing * (r+1)); y++) {
							count++;
							black += brightness(thresholdPic.pixels[y * thresholdPic.width + x]) < 128 ? 1 : 0;
						}
					}
					grid[r][c] |= black < count / 2;
					if (grid[r][c]) {
						fill(255, 0, 0, 128);
						noStroke();
//						rect(Math.round(boxWidth() * c), Math.round(boxHeight() * r), boxWidth(), boxHeight());
					}
				}
			}
		}
		
		// Precompute the line colours that will be used
		
		if (colourImage != null) {
			colourPic = loadImage(colourImage);
			colourPic.loadPixels();
			
			colourGrid = new int[height / heightSpacing][width / widthSpacing];
			
			for (int r = 0; r < rows(); r++) {
				for (int c = 0; c < columns(); c++) {
					float red = 0;
					float g = 0;
					float b = 0;
					int count = 0;
					
					for (int x = (int) Math.round(c * widthSpacing); x < Math.round((c+1) * widthSpacing); x++) {
						for (int y = (int) Math.round(r * heightSpacing); y < Math.round((r+1) * heightSpacing); y++) {
							int pixel = colourPic.pixels[y * width + x];
							red += red(pixel);
							g += green(pixel);
							b += blue(pixel);
							count++;
						}
					}
					
					colourGrid[r][c] = color(red / count, g / count, b / count);
					
//					drawBox(r, c, colourGrid[r][c]);
				}
			}
		}
	}
	
	public void draw() {
		if (keyPressed) {
			if (key == ' ') {
				pause = !pause;
			}
		}
		
		if (!pause) {
			stroke(0);
			strokeWeight(strokeWidth);
			
			boolean done = true;
			
			for (int i = 0; i < nLines; i++) {
				if (lines[i] != null && !lines[i].forwardDraw()) {
					lines[i] = newLine(stepSpeed[i], drawSpeed[i]);
				} else {
					done = false;
				}
			}
			
			if (done) {
				
			}
		}
	}
	
	public void drawLine(int gr, int gc, int grr, int gcc) {
		float px = scale(gc, columns(), width);
		float py = scale(gr, rows(), height);
		float pxx = scale(gcc, columns(), width);
		float pyy = scale(grr, rows(), height);
		
		if (colourImage != null) {
			float pxm = (px + pxx) / 2.0f;
			float pym = (py + pyy) / 2.0f;
			stroke(colourGrid[gr][gc]);
			line(px, py, pxm, pym);
			stroke(colourGrid[grr][gcc]);
			line(pxm, pym, pxx, pyy);
		} else {
			line(px, py, pxx, pyy);
		}
	}
	
	public void drawBox(int gr, int gc) {
		drawBox(gr, gc, color(255, 225, 225));
	}
	
	public void drawBox(int gr, int gc, int color) {
		float px = scale(gc, columns(), width) - (widthSpacing / 2);
		float py = scale(gr, rows(), height) - (heightSpacing / 2);
		
		noStroke();
		fill(color);
		rect(px, py, widthSpacing, heightSpacing);
	}
	
	public float scale(int gg, int gmax, int smax) {
		return (2 * gg + 1) * ((float) smax / gmax) / 2.0f;
	}
	
	public Line newLine(int stepSpeed, int drawSpeed) {
		int gr = 0;
		int gc = 0;
		int gd = -1;
		
		// This loops will break when there are no more places to try and place lines.
		
		while (gd == -1) {
			do {
				if (crem.size() == 0) {
					return null;
				}
				
				int idx = (int) random(crem.size()-1 - 1e-7f);
				gr = rrem.remove(idx);
				gc = crem.remove(idx);
			} while (grid[gr][gc]);
			
	//		drawBox(gr, gc);
			
			// Pick a random direction; the first highest directional value that does not make an illegal move. 
			
			int highestValue = -1;
			int highestDirection = -1;
			
			for (int d : dlist) {
				if (direction[d] > highestValue) {
					int nr = gr + dr[d];
					int nc = gc + dc[d];
					
					if (!invalidMove(gr, gc, nr, nc)) {
						highestValue = direction[d];
						highestDirection = d;
					}
				}
			}
			
			gd = highestDirection;
			
			Collections.shuffle(dlist);
		}
		
		return new Line(gr, gc, gd, stepSpeed, drawSpeed);
	}
	
	class Line {
		int cr;
		int cc;
		int cd;
		
		int stepSpeed;
		int drawSpeed;
		
		public Line(int br, int bc, int bd, int stepSpeed, int drawSpeed) {
			cr = br;
			cc = bc;
			cd = bd;
			
			this.stepSpeed = stepSpeed;
			this.drawSpeed = drawSpeed;
		}
		
		boolean forward() {
			for (int i = 0; i < stepSpeed; i++) {
				if (!forwardOnce()) {
					return false;
				}
			}
			
			return true;
		}
		
		boolean forwardOnce() {
			int nAttempts = 3;
			
			// If lineIntelligence is 0, then don't bother trying any collision avoidance
			
			if (lineIntelligence == 0) {
				nAttempts = 1;
			}
			
			int rotate = 0;
			
			// This is a state machine:
			// On attempt 0, depending on a die roll, attempt to go straight OR skip immediately to attempt 1
			// On attempt 1, attempt to rotate a random direction based on a coin flip
			// On attempt 2, attempt to rotate the other direction
			// Attempt 3 happens if attempt 0 is skipped and is fallen through to
			// Otherwise fail
			
			for (int attempts = 0; attempts < nAttempts; attempts++) {
				int nd = cd;
				
				if (attempts == 0) {
					double rand = random(1);
					
					if (rand >= straightProb || direction[nd] == DIR_AVOID) {
						// The random choice says we must turn
						attempts++;
						
						// unless turning is bad, then we'll try going straight later.
						if (lineIntelligence >= 1) {
							nAttempts++;
						}
					}
				}
				
				if (attempts == 1) {
					// Rotate one direction on the second attempt
					rotate = ((int) random(2)) * 2 - 1;
					nd += rotate;
				} else if (attempts == 2) {
					// Rotate the opposite direction on the third attempt
					nd -= rotate;
				} else if (attempts == 3) {
					// Finally try going straight if intelligence is high enough
					// nop
				}
				
				if (nd < 0) {
					nd += dr.length;
				} else if (nd >= dr.length) {
					nd -= dr.length;
				}
				
				if (direction[nd] == DIR_DISALLOWED) {
					continue;
				}
				
				int nr = cr + dr[nd];
				int nc = cc + dc[nd];
				
				if (invalidMove(nr, nc)) {
					// if lineIntelligence is greater than zero then attempt to find a new path if the suggested path does not work
					
					if (lineIntelligence >= 1) {
						continue;
					}
					
					break;
				}
				
				// if lineIntelligence is greater than 1 then try to prevent choosing a path that will inevitably lead into a dead end
				
				if (attempts < nAttempts-1 && lineIntelligence >= 2) {
					boolean okay = false;
					
					for (int i = -1; i <= 1; i++) {
						int nnd = nd + i;
						
						if (nnd < 0) {
							nnd += 8;
						} else if (nnd >= 8) {
							nnd -= 8;
						}
						
						int nnr = nr + dr[nnd];
						int nnc = nc + dc[nnd];
						
						if (!FortyFive.this.invalidMove(nr, nc, nnr, nnc)) {
							okay = true;
						}
					}
					
					if (!okay) {
						continue;
					}
				}
				
				grid[cr][cc] = true;
				grid[nr][nc] = true;
				
//				drawBox(nr, nc);
				
				cr = nr;
				cc = nc;
				cd = nd;
				
				return true;
			}
			
			return false;
		}
		
		
		boolean invalidMove(int nr, int nc) {
			return FortyFive.this.invalidMove(cr, cc, nr, nc);
		}
		
		boolean forwardDraw() {
			for (int i = 0; i < drawSpeed; i++) {
				int r = cr;
				int c = cc;
				
				if (!forward()) {
					return false;
				}
				
				drawLine(r, c, cr, cc);
			}
			
			return true;
		}
	}
	
	public boolean invalidMove(int cr, int cc, int nr, int nc) {
		boolean invalid = nr < 0 || nc < 0 || nr >= rows() || nc >= columns() || grid[nr][nc];
		
		if (invalid) {
			return invalid;
		}
		
		invalid |= cr != nr && cc != nc && grid[nr][cc] && grid[cr][nc];
		
		return invalid;
	}
	
	int rows() {
		return height / heightSpacing;
	}
	
	int columns() {
		return width / widthSpacing;
	}
}
