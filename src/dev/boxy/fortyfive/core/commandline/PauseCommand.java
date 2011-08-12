package dev.boxy.fortyfive.core.commandline;

import dev.boxy.fortyfive.*;

public class PauseCommand implements FortyFiveCommand {

	public void execute() {
		FortyFive.getInstance().togglePause();
	}

}
