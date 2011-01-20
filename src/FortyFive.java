import java.util.*;

import processing.core.*;

public class FortyFive extends PApplet {
	
	public enum Settings {
		
		DANSE_MID (540, 720, 240, 180, 1, 5, 1, 2, "/Users/boxysean/Desktop/fortyfive/danse-mid.jpg"),
		DANSE_BLACK (540, 720, 720, 540, 1, 50, 1, 0, "/Users/boxysean/Desktop/fortyfive/danse-black.jpg"),
		DANSE_BLACK_DIAG (540, 720, 720, 540, 1, 50, 1, 0, "/Users/boxysean/Desktop/fortyfive/danse-black.jpg"),
		DANSE_WHITE (540, 720, 240, 180, 1, 5, 1, 2, "/Users/boxysean/Desktop/fortyfive/danse-white.jpg"),
		SUNSET (600, 399, 399, 600, 1, 10, 1, 2, "/Users/boxysean/Desktop/fortyfive/P1010475.JPG"),
		SMALL (100, 100, 10, 10, 1, 1, 1, 2, null),
		EYE (180, 360, 180, 90, 1, 5, 1, 0, "/Users/boxysean/Desktop/fortyfive/eye.jpg"),
		VANCOUVER (480, 640, 320, 240, 0.95, 5, 2, 1, "/Users/boxysean/Desktop/fortyfive/vancouver.jpg"),
		BRIDGE (800, 600, 150, 200, 0.9, 5, 2, 2, "/Users/boxysean/Desktop/fortyfive/20110114/bridge.jpg"),
		KETTLE (600, 800, 400, 300, 1, 5, 1, 0, "/Users/boxysean/Desktop/fortyfive/20110114/kettle.jpg"),
		RAVEN (600, 800, 160, 120, 0.75, 5, 2, 1, "/Users/boxysean/Desktop/fortyfive/20110114/raven.jpg"),
		HELICOPTER (600, 800, 80, 60, 0.75, 5, 2, 0, "/Users/boxysean/Desktop/fortyfive/20110114/helicopter.jpg"),
		DALAI (800, 600, 300, 400, 0.75, 5, 1, 2, "/Users/boxysean/Desktop/fortyfive/20110114/dalai.jpg");
		
		int width;
		int height;
		int gridRows;
		int gridColumns;
		double straightProb;
		int nLines;
		int strokeWidth;
		int lineIntelligence;
//		String thresholdImage;
		String colourImage;
		
		private Settings(int width, int height, int gridRows, int gridColumns, double straightProb, int nLines, int strokeWidth, int lineIntelligence, String colourImage) {
			this.width = width;
			this.height = height;
			this.gridRows = gridRows;
			this.gridColumns = gridColumns;
			this.straightProb = straightProb;
			this.nLines = nLines;
			this.strokeWidth = strokeWidth;
			this.lineIntelligence = lineIntelligence;
//			this.thresholdImage = loadImage;
			this.colourImage = colourImage;
		}
	}
	
	// 0 = top, 1 = top right, ..., 7 = top left
	public static final int[] dr = new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	public static final int[] dc = new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	
	public static final int[] direction = new int[] { 2, 2, 0, 0, 2, 0, 0, 2 };
	
	int gridRows = 240;
	int gridColumns = 180;
	double straightProb = 0.80;
	int nLines = 5;
	int strokeWidth = 1;
	int lineIntelligence = 2;
//	String thresholdImage = null;
	String colourImage = null;
	
	boolean[][] grid;
	int[][] colourGrid;
	
	LinkedList<Integer> rrem = new LinkedList<Integer>();
	LinkedList<Integer> crem = new LinkedList<Integer>();
	List<Integer> dlist = new LinkedList<Integer>();
	
	PImage thresholdPic;
	PImage colourPic;
	
	Line[] lines = new Line[nLines];

	public void loadSettings(Settings s) {
		size(s.width, s.height);
		gridRows = s.gridRows;
		gridColumns = s.gridColumns;
		straightProb = s.straightProb;
		nLines = s.nLines;
		strokeWidth = s.strokeWidth;
		lineIntelligence = s.lineIntelligence;
		colourImage = s.colourImage;
		lines = new Line[nLines];
	}
	
	public void setup() {
		frameRate(30);
		
		loadSettings(Settings.HELICOPTER);
		
		background(255);
		
		grid = new boolean[gridRows][gridColumns];
		                              
		for (int r = 0; r < gridRows; r++) {
			for (int c = 0; c < gridColumns; c++) {
				rrem.add(r);
				crem.add(c);
			}
		}
		
		dlist.clear();
		
		for (int i = 0; i < 8; i++) {
			dlist.add(i);
		}
		
		Collections.shuffle(dlist);
		
		for (int i = 0; i < nLines; i++) {
			lines[i] = newLine();
		}
		
//		if (thresholdImage != null) {
//			thresholdPic = loadImage(thresholdImage);
//			size(thresholdPic.width, thresholdPic.height);
//			filter(BLUR);
//			filter(THRESHOLD);
//			
//			thresholdPic.loadPixels();
//			
//			for (int r = 0; r < gridRows; r++) {
//				for (int c = 0; c < gridColumns; c++) {
//					int count = 0;
//					int black = 0;
//					for (int x = (int) Math.round(boxWidth() * c); x < Math.round(boxWidth() * (c+1)); x++) {
//						for (int y = (int) Math.round(boxHeight() * r); y < Math.round(boxHeight() * (r+1)); y++) {
//							count++;
//							black += brightness(thresholdPic.pixels[y * thresholdPic.width + x]) < 128 ? 1 : 0;
//						}
//					}
//					grid[r][c] |= black < count / 2;
//					if (grid[r][c]) {
//						fill(255, 0, 0, 128);
//						noStroke();
////						rect(Math.round(boxWidth() * c), Math.round(boxHeight() * r), boxWidth(), boxHeight());
//					}
//				}
//			}
//		}
		
		if (colourImage != null) {
			colourPic = loadImage(colourImage);
			colourPic.loadPixels();
			
			colourGrid = new int[gridRows][gridColumns];
			
			for (int r = 0; r < gridRows; r++) {
				for (int c = 0; c < gridColumns; c++) {
					float red = 0;
					float g = 0;
					float b = 0;
					int count = 0;
					
					for (int x = (int) Math.round(c * boxWidth()); x < Math.round((c+1) * boxWidth()); x++) {
						for (int y = (int) Math.round(r * boxHeight()); y < Math.round((r+1) * boxHeight()); y++) {
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
	
	boolean pause = false;
	
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
					lines[i] = newLine();
				} else {
					done = false;
				}
			}
		}
	}
	
	public void drawLine(int gr, int gc, int grr, int gcc) {
		float px = scale(gc, gridColumns, width);
		float py = scale(gr, gridRows, height);
		float pxx = scale(gcc, gridColumns, width);
		float pyy = scale(grr, gridRows, height);
		
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
		float px = scale(gc, gridColumns, width) - (boxWidth() / 2);
		float py = scale(gr, gridRows, height) - (boxHeight() / 2);
		
		noStroke();
		fill(color);
		rect(px, py, boxWidth(), boxHeight());
	}
	
	public float scale(int gg, int gmax, int smax) {
		return (2 * gg + 1) * ((float) smax / gmax) / 2.0f;
	}
	
	public Line newLine() {
		int gr;
		int gc;
		
		do {
			if (crem.size() == 0) {
				return null;
			}
			
			int idx = (int) random(crem.size()-1 - 1e-7f);
			gr = rrem.remove(idx);
			gc = crem.remove(idx);
		} while (grid[gr][gc]);
		
//		drawBox(gr, gc);
		
		return new Line(gr, gc);
	}
	
	class Line {
		
		boolean brandNew = true;
		
		int cr;
		int cc;
		int cd;
		
		int speed1 = 1;
		int speed2 = 40;
		
		public Line(int br, int bc) {
			cr = br;
			cc = bc;
			
			for (int d : dlist) {
				if (direction[d] == 2) {
					continue;
				}
				
				int nr = cr + dr[d];
				int nc = cc + dc[d];
				
				if (!invalidMove(nr, nc)) {
					cd = d;
				}
			}
			
			Collections.shuffle(dlist);
		}
		
		boolean rotate[] = new boolean[3];
		
		boolean forward() {
			for (int i = 0; i < speed1; i++) {
				if (!forwardOnce()) {
					return false;
				}
			}
			
			return true;
		}
		
		boolean forwardOnce() {
			Arrays.fill(rotate, true);
			
			int nAttempts = 3;
			
			if (lineIntelligence == 0) {
				nAttempts = 1;
			}
			
			nAttempts = 3;
			int firstTry = -1;
			
			for (int attempts = 0; attempts < nAttempts; attempts++) {
				int nd = cd;
				
//				if (direction[nd] != 2) {
//					attempts++;
//				}
//				
//				if (attempts == 1) {
//					int xx = 2 * (int) Math.round(random(1)) - 1;
//					firstTry = xx;
//					nd += xx;
//				} else {
//					nd += firstTry * -1;
//				}
				
				if (attempts == 0) {
					double rand = random(1);
					// First pass: vanilla.
					
//					if (direction[nd])
					
//					while (true) {
//						nd++;
//						
//						if (nd >= 8) {
//							nd -= 8;
//						} else if (nd < 0) {
//							nd += 8;
//						}
//						
//						if (direction[nd] == 0) {
//							
//						}
//					}
					
//					while (direction[++nd] != 2) {
//					}
					
//					if (rand < straightProb) {
//						rotate[1] = false;
//					} else if (rand < straightProb + ((1 - straightProb) / 2.0)) {
//						nd = cd + 1;
//						rotate[2] = false;
//					} else {
//						nd = cd - 1;
//						rotate[0] = false;
//					}
				} else if (attempts == 1) {
					int idx = (int) random(2);
					
					// If idx == 0, try the first rotate you see. Otherwise, try the second rotate.
					
					for (int i = 0; i < 3; i++) {
						if (rotate[i]) {
							if (idx == 0) {
								nd = cd + i - 1;
								rotate[i] = false;
								break;
							} else {
								idx--;
							}
						}
					}
				} else if (attempts == 2) {
					for (int i = 0; i < 3; i++) {
						if (rotate[i]) {
							nd = cd + i - 1;
							rotate[i] = false;
							break;
						}
					}
				}
				
				if (nd < 0) {
					nd += dr.length;
				} else if (nd >= dr.length) {
					nd -= dr.length;
				}
				
				int nr = cr + dr[nd];
				int nc = cc + dc[nd];
				
				if (invalidMove(nr, nc)) {
//					LINE_INTELLIGENCE greater than zero means it will attempt to find a new path if the suggested path does not work
					
					if (lineIntelligence >= 1) {
						continue;
					}
					
					break;
				}
				
				// LINE_INTELLIGENCE greater than 1 means it will try to prevent choosing a path that will inevitably lead into a dead end
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
						
						if (!invalidMove(nr, nc, nnr, nnc)) {
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
			return invalidMove(cr, cc, nr, nc);
		}
		
		boolean invalidMove(int r, int c, int nr, int nc) {
			boolean invalid = nr < 0 || nc < 0 || nr >= gridRows || nc >= gridColumns || grid[nr][nc];
			
			if (invalid) {
				return invalid;
			}
			
			invalid |= cr != nr && cc != nc && grid[nr][cc] && grid[cr][nc];
			
			return invalid;
		}
		
		boolean forwardDraw() {
			for (int i = 0; i < speed2; i++) {
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
	
	float boxWidth() {
		return (float) width / gridColumns;
	}
	
	float boxHeight() {
		return (float) height / gridRows;
	}
	
}
