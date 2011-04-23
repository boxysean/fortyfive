package dev.boxy.fortyfive;

import java.util.*;

/**
 * Speed up load times on image grid objects (roughly 0.5s without cache)
 * @author boxysean
 *
 */
public class ImageGridCache {
	
	Map<Record, ImageGrid> cache = new HashMap<Record, ImageGrid>();
	
	public ImageGrid get(FortyFive ff, String imageFile, String configFile) {
		Record rec = new Record(imageFile, configFile);
		
		ImageGrid res = cache.get(rec);
		
		if (res == null) {
			res = new ImageGrid(ff, imageFile);
			cache.put(rec, res);
		}
		
		return res;
	}
	
	class Record {
		String imageFile;
		String configFile;
		
		public Record(String imageFile, String configFile) {
			this.imageFile = imageFile;
			this.configFile = configFile;
		}
		
		@Override
		public int hashCode() {
			return imageFile.hashCode() * 31 + configFile.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			try {
				Record rec = (Record) o;
				return imageFile.equals(rec.imageFile) && configFile.equals(rec.configFile);
			} catch (Exception e) {
				return false;
			}
		}
	}
	
}
