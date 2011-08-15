package dev.boxy.fortyfive.core.draw;

import dev.boxy.fortyfive.core.scene.*;

public interface LineDrawFactory {

	public LineDraw get(Scene scene);
	public String getName();
	
}
