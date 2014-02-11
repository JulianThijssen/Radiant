package com.radiant;

import obj.radiant.exceptions.RadiantException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL30;

public abstract class BasicGame implements Game {
	private String title;
	private DisplayMode currentDisplayMode;
	
	protected Scene scene = new Scene();
	
	protected boolean running = false;
	
	public BasicGame(String title) {
		this.title = title;
	}
	
	public final void setTitle(String title) {
		this.title = title;
	}
	
	public final void setSize(int width, int height) {
		currentDisplayMode = new DisplayMode(width, height);
	}
	
	public final void start() throws RadiantException {
		
		try {
			Display.setTitle(title);
			Display.setDisplayMode(currentDisplayMode);
			Display.create();
		} catch(LWJGLException e) {
			throw new RadiantException(e.getMessage());
		}
		GL30.glGenVertexArrays();
		running = true;
		update();
	}

	@Override
	public void stop() {
		running = false;
	}
}
