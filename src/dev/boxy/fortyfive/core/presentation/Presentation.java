package dev.boxy.fortyfive.core.presentation;


public interface Presentation {

	public void snapshot();
	public void onFinished();
	public String getCurrentFile();
	public void onLoadFail();
	public void resetLoadFails();
	public void nextFrame();
	public void next(int x);
	
}
