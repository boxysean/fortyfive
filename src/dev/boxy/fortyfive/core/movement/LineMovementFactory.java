package dev.boxy.fortyfive.core.movement;

import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.utils.*;

/**
 * @defgroup movements movements
 * Indicate which movements with tag type
 */

public interface LineMovementFactory extends ConfigLoader {

	public LineMovement get(Scene scene, Line line);
	public String getName();
	public int getDirection(int id);
	
}
