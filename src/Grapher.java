import processing.core.*;

public class Grapher extends PApplet {

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "Grapher" });
	}

	// keep track of time at front and back and plot based on time!
	// stationary labels at the bottom that change to absolute when paused
	// scrollable!!
	// not based on draw calls... :(
	// paint between canvases
	// resizable -- lengthwise
	// zoomable

	int SCREEN_WIDTH = 640;
	int SCREEN_HEIGHT = 440;

	int CHART_WIDTH = 600;
	int CHART_HEIGHT = 400;

	int CHART_OFFSET_X = 20;
	int CHART_OFFSET_Y = 20;

	int FRAMERATE = 60;

	int fc = 0;

	PGraphics[] pg;
	int pgx[] = new int[] { 0, 0 };
	int canvas;

	PGraphics rightOverlay;
	PGraphics bottomOverlay;

//	PFont font;

	boolean stop;

	public void setup() {
		size(SCREEN_WIDTH, SCREEN_HEIGHT, P2D);
		background(0);

//		font = loadFont("Monaco-12.vlw");

		frameRate(FRAMERATE);

		pg = new PGraphics[] { createGraphics(CHART_WIDTH, CHART_HEIGHT, P2D),
				createGraphics(CHART_WIDTH, CHART_HEIGHT, P2D) };

		rightOverlay = createGraphics(100, SCREEN_HEIGHT, P2D);
		bottomOverlay = createGraphics(SCREEN_WIDTH, 100, P2D);

		for (int i = 0; i < pg.length; i++) {
			pg[i].beginDraw();
			pg[i].background(0, 0, 0);
			pg[i].endDraw();
//			pg[i].textFont(font, 12);
		}

		colorMode(RGB, 255);
		stroke(RGB, 255);
		color(RGB, 255);

		createRightOverlay();
		createBottomOverlay();

		canvas = 0;

		stop = false;
	}

	public void draw() {

		if (!stop) {

			if (fc % CHART_WIDTH == 0) {
				println(frameRate);
				canvas = 1 - canvas;
				pgx[canvas] = CHART_WIDTH;
				pg[canvas].beginDraw();
				pg[canvas].background(0);
				pg[canvas].endDraw();
			}

			if (CHART_OFFSET_X <= mouseX
					&& mouseX < CHART_WIDTH + CHART_OFFSET_X
					&& CHART_OFFSET_Y <= mouseY
					&& mouseY < CHART_HEIGHT + CHART_OFFSET_Y) {
				drawPoint(mouseX - CHART_OFFSET_X, mouseY - CHART_OFFSET_Y);
			}

			drawPoint(CHART_WIDTH + CHART_OFFSET_X, CHART_HEIGHT
					+ CHART_OFFSET_Y);

			if (fc % FRAMERATE == 0) {
				// fill(255);
				// drawText("joe", WIDTH-1, HEIGHT-5);
				// text("" + ((frameCount-1) / FRAMERATE));
				drawPoint(CHART_WIDTH, CHART_HEIGHT + CHART_OFFSET_Y - 1);
				drawPoint(CHART_WIDTH, CHART_HEIGHT + CHART_OFFSET_Y - 2);
				drawPoint(CHART_WIDTH, CHART_HEIGHT + CHART_OFFSET_Y - 3);
			}

			for (int i = 0; i < pg.length; i++) {
				image(pg[i], pgx[i] + CHART_OFFSET_X, CHART_OFFSET_Y);
				pgx[i]--;
			}

			image(rightOverlay, SCREEN_WIDTH - 100, 0);
			image(bottomOverlay, 0, SCREEN_HEIGHT - 100);

			fc++;
		}
	}

	public void drawPoint(int x, int y) {
		int drawCanvas = -1;

		for (int i = 0; i < pg.length; i++) {
			if (pgx[i] <= x && x < pgx[i] + CHART_WIDTH) {
				drawCanvas = i;
				break;
			}
		}

		pg[drawCanvas].beginDraw();
		pg[drawCanvas].stroke((x - pgx[drawCanvas]) % CHART_WIDTH, y, 0);
		pg[drawCanvas].point((x - pgx[drawCanvas]) % CHART_WIDTH, y);
		pg[drawCanvas].endDraw();
	}

	public void drawText(String s, int x, int y) {
		int drawCanvas = -1;

		for (int i = 0; i < pg.length; i++) {
			if (pgx[i] <= x && x < pgx[i] + CHART_WIDTH) {
				drawCanvas = i;
				break;
			}
		}

		pg[drawCanvas].beginDraw();
		pg[drawCanvas].text(s, (x - pgx[drawCanvas]) % WIDTH, y, 0);
		pg[drawCanvas].endDraw();
	}

	public void createRightOverlay() {
		rightOverlay.beginDraw();

		for (int y = CHART_HEIGHT + CHART_OFFSET_Y; y >= CHART_OFFSET_Y; y--) {
			rightOverlay.stroke(75, y, 0);
			rightOverlay.point(75, y);
		}

		for (int y = CHART_HEIGHT + CHART_OFFSET_Y; y >= CHART_OFFSET_Y; y -= 25) {
			for (int x = 74; x >= 71; x--) {
				rightOverlay.stroke(x, y, 0);
				rightOverlay.point(x, y);
			}
		}

		rightOverlay.endDraw();
	}

	public void createBottomOverlay() {
		bottomOverlay.beginDraw();
//		bottomOverlay.textFont(font, 12);
//		bottomOverlay.text("now", SCREEN_WIDTH - 25, 92);
		bottomOverlay.endDraw();
	}

	public void keyTyped() {
		if (key == ' ') {
			stop = !stop;
		}
	}

}
