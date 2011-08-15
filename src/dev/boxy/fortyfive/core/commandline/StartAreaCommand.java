package dev.boxy.fortyfive.core.commandline;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.core.line.*;
import dev.boxy.fortyfive.core.scene.*;

public class StartAreaCommand implements FortyFiveCommand {
	
	String name;
	
	public StartAreaCommand(String name) {
		this.name = name;
	}
	
	public void execute() {
		FortyFive ff = FortyFive.getInstance();
		Scene scene = ff.getScene();
		LineFactory lineFactory = scene.getLineFactory(name);
		ff.toggleLayer(lineFactory.getStartAreaLayer());
	}

}
