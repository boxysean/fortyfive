package dev.boxy.fortyfive.presentation;

import java.awt.event.*;

public abstract class PresentationMode implements KeyListener {

	public abstract void keyPressed(KeyEvent e);
	public abstract void keyReleased(KeyEvent e);
	public abstract void keyTyped(KeyEvent e);
	public abstract void snapshot();
	public abstract void onFinished();
	public abstract String getCurrentFile();
	public abstract void onLoadFail();
	public abstract void resetLoadFails();
	public abstract void nextFrame();
	
}
