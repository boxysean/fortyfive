package dev.boxy.fortyfive.core.commandline;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.scene.*;
import dev.boxy.fortyfive.core.startarea.*;

public class StartAreaCommand implements FortyFiveCommand {
	
	String name;
	
	public StartAreaCommand(String name) {
		this.name = name;
	}
	
	public void execute() {
		FortyFive ff = FortyFive.getInstance();
		Scene scene = ff.getScene();
		StartArea startArea = scene.getStartArea(name);
		ff.toggleLayer(startArea.getGridLayer());
	}

}
