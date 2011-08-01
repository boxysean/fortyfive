package dev.boxy.fortyfive.draw;

public interface LineDrawFactory {

	public static final LineDrawFactory DEFAULT = SolidDrawFactory.DEFAULT;
	
	public LineDraw get();
	
}
