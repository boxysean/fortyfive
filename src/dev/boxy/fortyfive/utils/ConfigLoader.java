package dev.boxy.fortyfive.utils;

import java.util.*;

import dev.boxy.fortyfive.core.scene.*;

public interface ConfigLoader {
	
	public void loadSettings(SceneFactory sceneFactory, Map<String, Object> map);
	
}
