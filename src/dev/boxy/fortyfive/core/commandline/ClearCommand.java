package dev.boxy.fortyfive.core.commandline;

import dev.boxy.fortyfive.*;

public class ClearCommand implements FortyFiveCommand {

	public void execute() {
		FortyFive.getInstance().clear();
	}

}
