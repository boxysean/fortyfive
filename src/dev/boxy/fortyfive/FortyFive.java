package dev.boxy.fortyfive;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.*;

import processing.core.*;
import dev.boxy.fortyfive.core.presentation.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class FortyFive extends PApplet {
	
	public static final boolean		DEBUG				= Boolean.getBoolean("DEBUG");
	public static final boolean		SHOW_THRESHOLD		= Boolean.getBoolean("SHOW_THRESHOLD");
	public static final boolean		SHOW_STARTAREA		= Boolean.getBoolean("SHOW_STARTAREA");
	
	public static int               ITERATIONS          = 0;
	public static int				FRAMES				= 0;
	
	private static FortyFive		INSTANCE			= null;
	
	public static FortyFive getInstance() {
		return INSTANCE;
	}
	
	// 0 = top, 1 = top right, ..., 7 = top left
	public static final int[]	dr		= new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	public static final int[]	dc		= new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	
	static String[] args;
	
	protected int userDrawSpeedMultiplier = 1;
	
	protected Map<String, SceneFactory> sceneFactories = new HashMap<String, SceneFactory>();
	
	protected Scene scene;
	
	protected boolean pause = false;
	
	public SceneFactory loadSettings(String configFile) throws Exception {
		if (sceneFactories.containsKey(configFile)) {
			return sceneFactories.get(configFile);
		}
		
		Yaml yaml = new Yaml();
		Logger logger = Logger.getInstance();

		Map<String, Object> map = null;
		
		try {
			FileReader fileReader = new FileReader(configFile);
			map = (Map<String, Object>) yaml.load(fileReader);
		} catch (FileNotFoundException e) {
			logger.error("load settings: file not found, %s\n", new File(configFile).getAbsolutePath());
			throw e;
		}
		
		// TODO these should almost be in the master template
		
		int width = ConfigParser.getInt(map, "width", screen.width);
		int height = ConfigParser.getInt(map, "height", screen.height);
		size(width, height);
		
		SceneFactory sceneFactory = new SceneFactory(map);
		
		sceneFactories.put(configFile, sceneFactory);
		
		return sceneFactory;
	}
	
	@Override
	public void setup() {
		INSTANCE = this;
		
		noCursor();
		
		Presentation.setMode(new LinearPresentation(this, args[0]));
		Presentation presentation = Presentation.getInstance();
		addKeyListener(presentation);
		
		setup(presentation.getCurrentFile());
	}
	
	String queuedConfig = null;
	
	public void queueConfig(String configFile) {
		queuedConfig = configFile;
		TimingUtils.reset();
	}
	
	private void setup(String configFile) {
		Logger logger = Logger.getInstance();
		
		logger.log("--- " + configFile + " ---");
		
		Presentation presentation = Presentation.getInstance();
		
		try {
			SceneFactory sceneFactory = loadSettings(configFile);
			scene = sceneFactory.get();
		} catch (Exception e) {
			// The presentation object takes care of the exception and we should try setting up again
			e.printStackTrace();
			presentation.onLoadFail();
			return;
		}
		
		presentation.resetLoadFails();
		
		scene.setup();
	}
	
	@Override
	public void draw() {
		if (queuedConfig != null) {
			// New config has been requested
			setup(queuedConfig);
			queuedConfig = null;
			FRAMES = 0;
		}
		
		Presentation presentation = Presentation.getInstance();
		
		if (!pause) {
			if (scene.draw()) {
				presentation.onFinished();
				ITERATIONS++;
			} else {
				presentation.nextFrame();
			}
		}
	}
	
	public void keyTyped(KeyEvent e) {
		char key = e.getKeyChar();
		
		if ('0' <= key && key <= '9') {
			userDrawSpeedMultiplier = key - '0';
		}
		
		switch (key) {
		case ' ':
			pause = !pause;
			break;
		}
	}
	
	public void keyPressed(KeyEvent e) {
	    int keyCode = e.getKeyCode();
	    
		switch (keyCode) {
		case KeyEvent.VK_UP:
			userDrawSpeedMultiplier++;
			
			break;
			
		case KeyEvent.VK_DOWN:
			userDrawSpeedMultiplier--;
			if (/*drawSpeedMultiplier + */userDrawSpeedMultiplier < 1) {
				userDrawSpeedMultiplier = 1;
			}
			
			break;
		}
	}
	
	public int getUserDrawSpeedMultiplier() {
		return userDrawSpeedMultiplier;
	}

	public static void main(String args[]) {
		FortyFive.args = args;
		
		PApplet.main(new String[] { "--present", "dev.boxy.fortyfive.FortyFive" });
	}
}
