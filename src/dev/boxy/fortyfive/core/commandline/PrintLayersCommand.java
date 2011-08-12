package dev.boxy.fortyfive.core.commandline;

import dev.boxy.fortyfive.*;
import dev.boxy.fortyfive.utils.*;

public class PrintLayersCommand implements FortyFiveCommand {

	public void execute() {
		int i = 0;
		
		for (FortyFiveLayer layer : FortyFive.getInstance().getLayers()) {
			Logger.getInstance().log("%03d: %s | %s", i++, layer.getClass().toString(), layer.toString());
		}
	}

}
