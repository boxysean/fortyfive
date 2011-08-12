package dev.boxy.fortyfive.core.commandline;

import dev.boxy.fortyfive.*;

public class NextCommand implements FortyFiveCommand {

	protected int x;
	
	public NextCommand() {
		this(1);
	}
	
	public NextCommand(int x) {
		this.x = x;
	}
	
	public void execute() {
		FortyFive.getInstance().getPresentation().next(x);
	}

}
