package dev.boxy.fortyfive;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.yaml.snakeyaml.*;

import processing.core.*;
import dev.boxy.fortyfive.core.commandline.*;
import dev.boxy.fortyfive.core.presentation.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class FortyFive extends PApplet {
	
	public static final boolean		DEBUG				= Boolean.getBoolean("DEBUG");
	public static final boolean		SHOW_THRESHOLD		= Boolean.getBoolean("SHOW_THRESHOLD");
	public static final boolean		SHOW_STARTAREA		= Boolean.getBoolean("SHOW_STARTAREA");
	
	public static int               ITERATIONS          = 0;
	public static int				FRAMES				= 0;
	
	// 0 = top, 1 = top right, ..., 7 = top left
	public static final int[]		dr					= new int[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	public static final int[]		dc					= new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	
	public static int				THREAD_POOL_SIZE	= 5;
	
	private static FortyFive		INSTANCE			= null;
	
	public static FortyFive getInstance() {
		return INSTANCE;
	}
	
	static String[] args;
	
	protected int userDrawSpeedMultiplier = 1;
	
	protected Map<String, SceneFactory> sceneFactories = new HashMap<String, SceneFactory>();
	
	protected Scene scene;
	
	protected boolean pause = false;
	
	protected List<FortyFiveLayer> layers = new ArrayList<FortyFiveLayer>();
	protected List<FortyFiveCommand> commands = new ArrayList<FortyFiveCommand>();
	
	protected ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	
	protected Presentation presentation;
	
	protected String queuedConfig = null;
	
	public FortyFive() {
		super();
		
		INSTANCE = this;
	}
	
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
		
		// TODO these should almost certainly be in the master template
		
		int width = ConfigParser.getInt(map, "width", screen.width);
		int height = ConfigParser.getInt(map, "height", screen.height);
		size(width, height);
		
		SceneFactory sceneFactory = new SceneFactory(map);
		
		sceneFactories.put(configFile, sceneFactory);
		
		return sceneFactory;
	}
	
	@Override
	public void setup() {
		noCursor();
		colorMode(ARGB);
		
		presentation = new LinearPresentation(this, args[0]);
		setup(presentation.getCurrentFile());
		
		CommandLine commandLine = new CommandLine();
	}
	
	public void clear() {
		queueConfig(presentation.getCurrentFile());
	}
	
	public void queueConfig(String configFile) {
		queuedConfig = configFile;
	}
	
	protected void setup(String configFile) {
		Logger logger = Logger.getInstance();
		
		logger.log("--- " + configFile + " ---");
		
		if (scene != null) {
			removeLayer(scene);
		}
		
		try {
			SceneFactory sceneFactory = loadSettings(configFile);
			scene = sceneFactory.get();
		} catch (Exception e) {
			// The presentation object takes care of the exception and we should try setting up again
			e.printStackTrace();
			presentation.onLoadFail();
			return;
		}
		
		addLayer(scene);
		
		presentation.resetLoadFails();
		
		scene.setup();
		
		queuedConfig = null;
		FRAMES = 0;
	}
	
	@Override
	public void draw() {
		if (queuedConfig != null) {
			// New config has been requested
			setup(queuedConfig);
		}
		
		noStroke();
		fill(0);
		rect(0, 0, width, height);
		
		synchronized (commands) {
			for (FortyFiveCommand command : commands) {
				command.execute();
			}
		
			commands.clear();
		}
		
		synchronized (layers) {
			for (FortyFiveLayer layer : layers) {
				layer.draw(g);
			}
		}
	}
	
	public void keyTyped(KeyEvent e) {
		char key = e.getKeyChar();
		
		if ('0' <= key && key <= '9') {
			userDrawSpeedMultiplier = key - '0';
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
	
	public List<FortyFiveLayer> getLayers() {
		return layers;
	}
	
	public void removeLayer(FortyFiveLayer layer) {
		synchronized (layers) {
			layers.remove(layer);
		}
	}
	
	public void addLayer(FortyFiveLayer layer) {
		synchronized (layers) {
			layers.add(layer);
			Collections.sort(layers, new FortyFiveLayerComparator());
		}
	}
	
	public void toggleLayer(FortyFiveLayer layer) {
		if (layers.contains(layer)) {
			removeLayer(layer);
		} else {
			addLayer(layer);
		}
	}
	
	public void togglePause() {
		pause = !pause;
	}
	
	public boolean isPaused() {
		return pause;
	}
	
	public void addCommand(FortyFiveCommand command) {
		synchronized (commands) {
			commands.add(command);
		}
	}
	
	public Presentation getPresentation() {
		return presentation;
	}
	
	public Scene getScene() {
		return scene;
	}
	
	public void addJob(Runnable r) {
		threadPool.submit(r);
	}
	
	public static void main(String args[]) {
		FortyFive.args = args;
		
		PApplet.main(new String[] { "--present", "dev.boxy.fortyfive.FortyFive" });
	}
}
