package dev.boxy.fortyfive.core.draw;

import dev.boxy.fortyfive.core.scene.*;

/**
 * @defgroup lineDraws lineDraws
 * Indicate which lineDraw with tag type
 */

public interface LineDrawFactory {

	public LineDraw get(Scene scene);
	public String getName();
	
}
