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

/**
 * \mainpage fortyfive
 * 
 * <a href="http://www.boxysean.com/projects/renegade-projections.html">Example output</a> of this project.
 * 
 * \section howto How to use this documentation
 * 
 * The program runs on settings in <a href="http://en.wikipedia.org/wiki/YAML">YAML</a> files fed into it. The parameters of the YAML files are detailed in the Modules tab.
 * 
 * \section example Example
 * 
 * To run the example:
 * 
 * \code
 * 
 * java FortyFive template.yaml
 * 
 * \endcode
 * 
 * Contents of <i>template.yaml</i>
 * 
 * \code
 * 
 * baseDir: ../configs
 * 
 * templates:
 *  - Simple.yaml
 *  - RedGreen.yaml
 * 
 * \endcode
 *
 * Contents of <i>Simple.yaml</i>
 * 
 * \code
 * 
 *
---
bgcolour:          black

widthSpacing:      10
heightSpacing:     10

areas:
  - name:          all
    x:             0
    y:             0
    width:         width
    height:        height

colours:
  - name:          red
    red:           255

  - name:          green
    green:         255

colourPalettes:
  - name:          red
    colours:       red

  - name:          green
    colours:       green

movements:
  - name:          A
    type:          IntelligentMovement
    intelligence:  2

coordBags:
  - name:          random
    type:          random

lineDraws:
  - &id001
    name:          A
    palette:       red
    strokeWidth:   5

  - <<: *id001
    name:          B
    palette:       green

lines:
  - name:          A
    draw:          A
    movement:      A
    startArea:     +all
    coordBag:      random

  - name:          B
    draw:          B
    movement:      A
    startArea:     +all
    coordBag:      random

deploy:            [A, B]

...

 *
 * \endcode
 *
 * Contents of <i>RedGreen.yaml</i>
 * 
 * \code
 * 
 * ---
bgcolour:        black

widthSpacing:    10
heightSpacing:   10

colours:
  - name:        red
    red:         255

  - name:        blue
    blue:        255

  - name:        green
    green:       255

colourPalettes:
  - name:        rgb
    mode:        linear
    colours:     [red, green, blue]

  - name:        gbr
    mode:        linear
    colours:     [green, blue, red]

coordBags:
  - name:        random
    type:        random

areas:
  - name:        right
    x:           0
    y:           0
    width:       15
    height:      height

  - name:        top
    x:           0
    y:           0
    width:       width
    height:      15

  - name:        left
    x:           -10
    y:           0
    width:       10
    height:      height

  - name:        bottom
    x:           0
    y:           -10
    width:       width
    height:      10

movements:
  - &id004
    name:          left
    type:          IntelligentMovement
    intelligence:  1
    straightProb:  1.0
    direction:     "00000121"

  - <<: *id004
    name:          top
    direction:     "21000001"

  - <<: *id004
    name:          bottom
    direction:     "00012100"

  - <<: *id004
    name:          right
    direction:     "01210000"

lineDraws:
  - &id001
    name:          top
    palette:       rgb
    strokeWidth:   10

  - <<:            *id001
    name:          bottom

  - <<:            *id001
    name:          right
    palette:       gbr

  - <<:            *id001
    name:          left
    palette:       gbr

lines:
  - &id002
    drawSpeed:     2
    name:          top
    draw:          top
    movement:      top
    coordBag:      random
    startArea:     +top

  - <<: *id002
    name:          bottom
    draw:          bottom
    movement:      bottom
    startArea:     +bottom

  - &id003 
    drawSpeed:     5
    name:          left
    draw:          left
    movement:      left
    coordBag:      random
    startArea:     +left

  - <<: *id003
    name:          right
    draw:          right
    movement:      right
    startArea:     +right

deploy: [top, bottom, right, left]

...

 * 
 * \endcode
 *
 */

public class FortyFive extends PApplet {
	
	public static final boolean		DEBUG				= Boolean.getBoolean("DEBUG");
	
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
