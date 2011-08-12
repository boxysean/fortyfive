package dev.boxy.fortyfive.core.commandline;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.image.*;
import dev.boxy.fortyfive.core.scene.*;

public class ThresholdCommand implements FortyFiveCommand {
	
	String name;
	
	public ThresholdCommand(String name) {
		this.name = name;
	}
	
	public void execute() {
		FortyFive ff = FortyFive.getInstance();
		Scene scene = ff.getScene();
		ImageThreshold imageThreshold = scene.getImageThreshold(name);
		ff.toggleLayer(imageThreshold.getGridLayer());
	}

}
