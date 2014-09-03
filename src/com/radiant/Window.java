package com.radiant;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import com.radiant.exceptions.RadiantException;
import com.radiant.util.Log;

public final class Window {
	public static final String DEFAULT_TITLE = "Radiant Game";
	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 600;
	
	private DisplayMode currentDisplayMode;
	
	public Window() {
		
	}
	
	public void create() throws RadiantException {
		create(DEFAULT_TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	public void create(String title) throws RadiantException {
		create(title, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	public void create(String title, int width, int height) throws RadiantException {
		try {
			setTitle(title);
			setSize(width, height);
			Display.create();
		} catch(LWJGLException e) {
			throw new RadiantException("Window context can not be created");
		}
	}
	
	public String getTitle() {
		return Display.getTitle();
	}
	
	public void setTitle(String title) {
		Display.setTitle(title);
	}
	
	public void setSize(int width, int height) {
		currentDisplayMode = new DisplayMode(width, height);
		try {
			Display.setDisplayMode(currentDisplayMode);
		} catch (LWJGLException e) {
			Log.debug("Failed to resize the window");
		}
	}
	
	public void update() {
		Display.update();
	}
	
	public boolean isClosed() {
		return Display.isCloseRequested();
	}
	
	public void destroy() {
		Display.destroy();
	}
}
