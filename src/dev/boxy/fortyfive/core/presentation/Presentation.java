package dev.boxy.fortyfive.core.presentation;

import java.awt.event.*;

public class Presentation implements KeyListener {
	
	static Presentation thePresentation;
	
	public static Presentation getInstance() {
		return thePresentation;
	}
	
	public static void setMode(PresentationMode mode) {
		thePresentation = new Presentation(mode);
	}
	
	

	public Presentation(PresentationMode mode) {
		this.mode = mode;
	}
	
	public PresentationMode mode;

	public void keyPressed(KeyEvent arg0) {
		mode.keyPressed(arg0);
	}

	public void keyReleased(KeyEvent arg0) {
		mode.keyReleased(arg0);
	}

	public void keyTyped(KeyEvent arg0) {
		mode.keyTyped(arg0);
	}
	
	public void snapshot() {
		mode.snapshot();
	}
	
	public void onFinished() {
		mode.onFinished();
	}
	
	public String getCurrentFile() {
		return mode.getCurrentFile();
	}
	
	public void onLoadFail() {
		mode.onLoadFail();
	}
	
	public void resetLoadFails() {
		mode.resetLoadFails();
	}
	
	public void nextFrame() {
		mode.nextFrame();
	}
	
}
