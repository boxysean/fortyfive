package dev.boxy.fortyfive.core.startarea;

import java.util.*;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.coordinatebag.*;
import dev.boxy.fortyfive.core.image.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

public class VectorStartAreaFactory implements ConfigLoader {
	
	public static final int IMAGE_THRESHOLD_FUDGE_FACTOR = 7; //pixels
	public static int FACTORY_ID = 1;
	
	protected SceneFactory sceneFactory;
	
	protected String name = getDefaultName();
	protected boolean blocked = false;
	protected List<StartAreaShape> shapes = new ArrayList<StartAreaShape>();
	protected String coordBagName;
	
	public VectorStartAreaFactory(SceneFactory sceneFactory, Map<String, Object> map) {
		this.sceneFactory = sceneFactory;
		loadSettings(sceneFactory, map);
	}
	
	public StartAreaFactoryJob get() {
		return new StartAreaFactoryJob(sceneFactory, name, blocked, shapes, coordBagName);
	}
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map) {
		FortyFive ff = FortyFive.getInstance();
		
		name = ConfigParser.getString(map, "name");
		blocked = ConfigParser.getBoolean(map, "blocked", new String[] { "all", "true" });
		coordBagName = ConfigParser.getString(map, "coordBag");
		
		if (map.containsKey("addRect")) {
			List<Object> coords = (List<Object>) map.get("addRect");
			
			int x = ConfigParser.parseInt(coords, 0, ff.getWidth(), 0);
			int y = ConfigParser.parseInt(coords, 1, ff.getHeight(), 0);
			int width = ConfigParser.parseInt(coords, 2, ff.getWidth(), ff.getWidth());
			int height = ConfigParser.parseInt(coords, 3, ff.getHeight(), ff.getHeight());
			
			shapes.add(new Rectangle(sceneFactory, x, y, width, height, false));
		}
		
		if (map.containsKey("addImage")) {
			List<Object> image = (List<Object>) map.get("addImage");
			
			// Allow some fudge factor in the config for extra flexibility. Positive offsets shrink the side.
			
			String imageName = (String) image.get(0);
			int topOffset = ConfigParser.getInt(image, 1, 0);
			int rightOffset = ConfigParser.getInt(image, 2, 0);
			int bottomOffset = ConfigParser.getInt(image, 3, 0);
			int leftOffset = ConfigParser.getInt(image, 4, 0);
			
			ImageThreshold imageThreshold = sceneFactory.getImageThreshold(imageName);
			
			if (imageThreshold != null) {
				int x = imageThreshold.getXOffset() + leftOffset;
				int y = imageThreshold.getYOffset() + topOffset;
				int width = (int) imageThreshold.getWidth() - leftOffset - rightOffset;
				int height = (int) imageThreshold.getHeight() - topOffset - bottomOffset;
				
				shapes.add(new Rectangle(sceneFactory, x + IMAGE_THRESHOLD_FUDGE_FACTOR, y + IMAGE_THRESHOLD_FUDGE_FACTOR, width - IMAGE_THRESHOLD_FUDGE_FACTOR - IMAGE_THRESHOLD_FUDGE_FACTOR, height - IMAGE_THRESHOLD_FUDGE_FACTOR - IMAGE_THRESHOLD_FUDGE_FACTOR, false));
			}
		}
		
		if (map.containsKey("removeRect")) {
			List<Object> coords = (List<Object>) map.get("removeRect");
			
			int x = ConfigParser.parseInt(coords, 0, ff.getWidth(), 0);
			int y = ConfigParser.parseInt(coords, 1, ff.getHeight(), 0);
			int width = ConfigParser.parseInt(coords, 2, ff.getWidth(), ff.getWidth());
			int height = ConfigParser.parseInt(coords, 3, ff.getHeight(), ff.getHeight());
			
			shapes.add(new Rectangle(sceneFactory, x, y, width, height, true));
		}
		
		if (map.containsKey("removeImage")) {
			List<Object> image = (List<Object>) map.get("removeImage");
			
			String imageName = (String) image.get(0);
			int topOffset = ConfigParser.getInt(image, 1, 0);
			int rightOffset = ConfigParser.getInt(image, 2, 0);
			int bottomOffset = ConfigParser.getInt(image, 3, 0);
			int leftOffset = ConfigParser.getInt(image, 4, 0);
			
			ImageThreshold imageThreshold = sceneFactory.getImageThreshold(imageName);
			
			if (imageThreshold != null) {
				int x = imageThreshold.getXOffset() + leftOffset;
				int y = imageThreshold.getYOffset() + topOffset;
				int width = (int) imageThreshold.getWidth() - leftOffset - rightOffset;
				int height = (int) imageThreshold.getHeight() - topOffset - bottomOffset;
				
				shapes.add(new Rectangle(sceneFactory, x + IMAGE_THRESHOLD_FUDGE_FACTOR, y + IMAGE_THRESHOLD_FUDGE_FACTOR, width - IMAGE_THRESHOLD_FUDGE_FACTOR - IMAGE_THRESHOLD_FUDGE_FACTOR, height - IMAGE_THRESHOLD_FUDGE_FACTOR - IMAGE_THRESHOLD_FUDGE_FACTOR, true));
			}
		}
	}
	
	protected static String getDefaultName() {
		return String.format("default_%05d", FACTORY_ID++);
	}
	
}