package dev.boxy.fortyfive.core.commandline;

import java.awt.event.*;

import processing.core.*;
import dev.boxy.fortyfive.*;

public class CommandLine implements FortyFiveLayer, KeyListener {
	
	protected static final char COMMAND_CHAR = '/';
	
	protected static final int GRAPHICS_HEIGHT = 20;
	
	protected boolean inputMode;
	protected String inputString;
	
	protected PGraphics graphics;
	protected PFont font = FortyFive.getInstance().loadFont("../data/Monaco-16.vlw");
	
	public CommandLine() {
		FortyFive ff = FortyFive.getInstance();
		graphics = ff.createGraphics(ff.width, GRAPHICS_HEIGHT, PApplet.P2D);
		ff.addKeyListener(this);
	}

	public void keyPressed(KeyEvent ke) {
		switch (ke.getKeyCode()) { 
		case KeyEvent.VK_ESCAPE:
			if (inputMode) {
				inputMode = false;
				inputString = "";
				updateString();
				FortyFive.getInstance().removeLayer(this);
			}
			break;
			
		case KeyEvent.VK_BACK_SPACE:
		case KeyEvent.VK_DELETE:
			if (inputMode && inputString.length() > 0) {
				inputString = inputString.substring(0, inputString.length()-1);
			}
			break;
		}
	}

	public void keyReleased(KeyEvent ke) {
	}

	public void keyTyped(KeyEvent ke) {
		if (!inputMode) {
			switch (ke.getKeyChar()) {
			case COMMAND_CHAR:
				inputMode = true;
				inputString = "" + COMMAND_CHAR;
				updateString();
				break;
			}
			
			if (inputMode) {
				FortyFive.getInstance().addLayer(this);
			}
		} else {
			switch (ke.getKeyChar()) {
			case '\n':
				inputMode = false;
				command(inputString);
				break;
				
			case KeyEvent.VK_BACK_SPACE:
			case KeyEvent.VK_DELETE:
				break;
				
			default:
				inputString += ke.getKeyChar();
				break;
			}
			
			updateString();
			
			if (!inputMode) {
				FortyFive.getInstance().removeLayer(this);
			}
		}
	}
	
	protected void command(String t) {
		String split[] = t.split("\\s+");
		String s = split[0];
		
		if (s.equals("/pause")) {
			addCommand(new PauseCommand());
		} else if (s.equals("/snapshot")) {
			addCommand(new SnapshotCommand());
		} else if (s.equals("/clear")) {
			addCommand(new ClearCommand());
		} else if (s.equals("/next")) {
			addCommand(new NextCommand(1));
		} else if (s.equals("/previous")) {
			addCommand(new NextCommand(-1));
		} else if (s.equals("/threshold")) {
			addCommand(new ThresholdCommand(split[1]));
		} else if (s.equals("/layers")) {
			addCommand(new PrintLayersCommand());
		} else if (s.equals("/startarea")) {
			addCommand(new StartAreaCommand(split[1]));
		}
	}
	
	public void updateString() {
		// TODO synchronization problem here?
		graphics.beginDraw();
		
		graphics.background(0);
		graphics.fill(255);
		
		graphics.textFont(font);
		graphics.text(inputString, 0, 16);
		
		graphics.endDraw();
	}
	
	public void draw(PGraphics g) {
		g.image(graphics, 0, g.height - GRAPHICS_HEIGHT);
	}

	public int getOrder() {
		return 1;
	}
	
	protected void addCommand(FortyFiveCommand command) {
		FortyFive.getInstance().addCommand(command);
	}
	
}
