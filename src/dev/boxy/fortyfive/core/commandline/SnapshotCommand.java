package dev.boxy.fortyfive.core.commandline;

import dev.boxy.fortyfive.*;

public class SnapshotCommand implements FortyFiveCommand {

	public void execute() {
		FortyFive.getInstance().getPresentation().snapshot();
	}

}
