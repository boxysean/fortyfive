package dev.boxy.fortyfive;

import java.awt.event.*;

public class LinearPresentation implements KeyListener {
	
	public static final int MODE_REPEAT = 0;
	public static final int MODE_LINEAR = 1;
	public static final int MODE_RANDOM = 2;
	
	int mode;
	
	FortyFive ff;
	
	String[] configFiles = {
			"BlueGreenTop.yaml",
			"BlueGreenBottom.yaml",
			"BlueGreenLeft.yaml",
			"BlueGreenRight.yaml",
			"SpaceJam01.yaml",
			"SpaceJam02.yaml",
			"SpaceJam03.yaml",
			"SpaceJam04.yaml",
			"GrowthTop.yaml",
			"GrowthSides.yaml",
			"RedGreen.yaml",
			"RedGreenDiag.yaml",
			"Eric.yaml",
	};
	
	int idx = 0;
	int loadFails = 0;
	
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
	
	public String getCurrentFile() {
		return configFiles[idx];
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
		switch (mode) {
		case MODE_REPEAT:
			apply();
			break;
			
		case MODE_LINEAR:
			next();
			break;
			
		case MODE_RANDOM:
			idx = (int) ff.random(configFiles.length);
			apply();
			break;
		}
	}
	
	public void onLoadFail() {
		if (loadFails++ >= configFiles.length) {
			System.err.println("Nothing's loading mcfly");
			System.exit(1);
		}
		
		next();
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
		switch (e.getKeyChar()) {
		case 'q':
			mode = MODE_REPEAT;
			break;
			
		case 'w':
			mode = MODE_LINEAR;
			break;
			
		case 'e':
			mode = MODE_RANDOM;
			break;
		}
	}
	
}
