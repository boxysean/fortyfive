package dev.boxy.fortyfive.core.coordinatebag;

import dev.boxy.fortyfive.utils.*;

/**
 * @defgroup coordinateBags coordinateBags
 * Indicate which coordinate bag with tag type
 */

public interface CoordinateBagFactory extends ConfigLoader {

	public CoordinateBag get();
	public String getName();
	
}
