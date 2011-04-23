package dev.boxy.fortyfive;

import java.awt.event.*;

public class LinearPresentation implements KeyListener {
	
	FortyFive ff;
	
	String[] configFiles = {
			"HeartExp.yaml",
			"Heart.yaml"
	};
	
	int idx = 0;
	
	public LinearPresentation(FortyFive ff) {
		this.ff = ff;
	}
	
	public void next() {
		idx++;
		
		int nFiles = configFiles.length;
		
		if (idx >= nFiles) {
			idx -= nFiles;
		}
		
		apply();
	}
	
	public void previous() {
		idx--;
		
		int nFiles = configFiles.length;
		
		if (idx < 0) {
			idx += nFiles;
		}
		
		apply();
	}
	
	public void onKey(char key) {
		if (key == 'n') {
			next();
		} else if (key == 'p') {
			previous();
		}
	}
	
	public void apply() {
		ff.queueConfig(configFiles[idx]);
	}
	
	public void onFinished() {
		apply();
	}
	
	public void keyPressed(KeyEvent e) {
	    int keyCode = e.getKeyCode();
	    
		switch (keyCode) {
		case KeyEvent.VK_RIGHT:
			next();
			
			break;
			
		case KeyEvent.VK_LEFT:
			previous();
			
			break;
		}
	}
	
	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
	
}
