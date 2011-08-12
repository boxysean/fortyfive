package dev.boxy.fortyfive.core.presentation;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.*;

import oscP5.*;
import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.utils.*;

public class LinearPresentation implements Presentation {
	
	public static final int MODE_REPEAT = 0;
	public static final int MODE_LINEAR = 1;
	public static final int MODE_RANDOM = 2;
	
	public static int snapshotId = 0;
	public static final boolean USE_OSC = Boolean.getBoolean("USEOSC");
	
	OscP5 oscP5;
	
	int mode;
	protected FortyFive ff;
	String baseDir;
	boolean snapshotAtEnd;
	int[] snapshots;
	String[] templates;
	int frameIdx = 0;
	int snapshotIdx = 0;
	int finishedPause = 0;
	int completeFrame = 0;
	
	protected int idx = 0;
	protected int loadFails = 0;
	
	public LinearPresentation(FortyFive ff, String yamlFile) {
		this.ff = ff;
		
		if (USE_OSC) {
			this.oscP5 = new OscP5(this, 8898);
		}
		
		try {
			parseYaml(yamlFile);
		} catch (FileNotFoundException e) {
			System.err.printf("error: file not found, %s\n", new File(yamlFile).getAbsoluteFile());
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void parseYaml(String yamlFile) throws IOException {
		Yaml yaml = new Yaml();
		
		Map<String, Object> map = (Map<String, Object>) yaml.load(new FileReader(yamlFile));
		
		baseDir = ConfigParser.getString(map, "baseDir", ".");
		finishedPause = ConfigParser.getInt(map, "finishedPause", 0);
		completeFrame = ConfigParser.getInt(map, "completeFrame", -1);
		
		// Parse snapshots
		
		List<Object> snapshotList = (List<Object>) map.get("snapshots");
		
		if (snapshotList != null) {
			Iterator<Object> snapshotIterator = snapshotList.iterator();
			
			while (snapshotIterator.hasNext()) {
				Object o = snapshotIterator.next();
				
				if (o instanceof String) {
					String s = (String) o;
					
					if (s.equalsIgnoreCase("end")) {
						snapshotAtEnd = true;
					}
					
					snapshotIterator.remove();
				}
			}
			
			snapshots = new int[snapshotList.size()];
			
			for (int i = 0; i < snapshotList.size(); i++) {
				snapshots[i] = (Integer) snapshotList.get(i);
			}
			
			Arrays.sort(snapshots);
		} else {
			snapshots = new int[0];
		}
		
		// Parse templates
		
		List<String> templateList = (List<String>) map.get("templates");
		
		templates = new String[templateList.size()];
		templateList.toArray(templates);
		
		idx = 0;
		loadFails = 0;
	}
	
	public void next() {
		next(1);
	}
	
	public void next(int x) {
		idx += x;
		
		int nFiles = templates.length;
		
		if (idx >= nFiles) {
			idx -= nFiles;
		} else if (idx < 0) {
			idx += nFiles;
		}
		
		if (idx >= nFiles || idx < 0) {
			idx %= nFiles;
		}
		
		apply();
	}
	
	public void previous() {
		idx--;
		
		int nFiles = templates.length;
		
		if (idx < 0) {
			idx += nFiles;
		}
		
		apply();
	}
	
	public String getCurrentFile() {
		return baseDir + File.separatorChar + templates[idx];
	}
	
	public void apply() {
		frameIdx = 0;
		snapshotIdx = 0;
		ff.queueConfig(getCurrentFile());
	}
	
	public void onFinished() {
		if (snapshotAtEnd) {
			snapshot();
		}
		
		try {
			Thread.sleep(finishedPause);
		} catch (Exception e) {
			
		}
		
		switch (mode) {
		case MODE_REPEAT:
			apply();
			break;
			
		case MODE_LINEAR:
			next();
			break;
			
		case MODE_RANDOM:
			idx = (int) ff.random(templates.length);
			apply();
			break;
		}
	}
	
	public void onLoadFail() {
		if (loadFails++ >= templates.length) {
			System.err.println("Nothing's loading mcfly");
			System.exit(1);
		}
		
		next();
	}
	
	public void snapshot() {
		Logger.getInstance().log("Snapshot!");
		ff.save(String.format("%05d.png", snapshotId++));
	}
	
	public void resetLoadFails() {
		loadFails = 0;
	}
	
	public void nextFrame() {
		if (snapshotIdx < snapshots.length && frameIdx == snapshots[snapshotIdx]) {
			snapshot();
			snapshotIdx++;
		}
		
		if (completeFrame > 0 && frameIdx == completeFrame) {
			next();
		}
		
		frameIdx++;
	}
	
	void oscEvent(OscMessage theOscMessage) {
		if (theOscMessage.typetag().equals("ssii")) {
			int x = theOscMessage.get(2).intValue();
			int y = theOscMessage.get(3).intValue();
//			ff.userDrawSpeedMultiplier = x / 10 + 1;
		} else {
			String s = theOscMessage.get(0).stringValue();
			if (s.equals("forward")) {
				next();
			} else if (s.equals("back")) {
				previous();
			} else if (s.equals("clear")) {
				apply();
			}
		}
	}
}
