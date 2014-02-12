package com.radiant;

import obj.radiant.exceptions.RadiantException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public abstract class BasicGame implements Game {
	private String title;
	private DisplayMode currentDisplayMode;
	
	protected Scene scene = null;
	
	protected boolean running = false;
	
	public BasicGame(String title) {
		this.title = title;
	}
	
	@Override
	public abstract void onCreate();
	
	public final void start() throws RadiantException {
		try {
			Display.setTitle(title);
			if(currentDisplayMode != null) {
				Display.setDisplayMode(currentDisplayMode);
			}
			Display.create();
		} catch(LWJGLException e) {
			throw new RadiantException(e.getMessage());
		}
		scene = new Scene();
		
		onCreate();
		
		running = true;
		update();
	}

	public final void stop() {
		running = false;
	}
	
	@Override
	public abstract void onDestroy();
	
	
	
	public final void setTitle(String title) {
		this.title = title;
	}
	
	public final void setSize(int width, int height) {
		currentDisplayMode = new DisplayMode(width, height);
	}
}
