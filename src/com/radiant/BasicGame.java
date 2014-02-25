package com.radiant;

import java.util.ArrayList;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import com.radiant.exceptions.RadiantException;

public abstract class BasicGame implements Game {
	public static final int DEFAULT_WIDTH = 640;
	public static final int DEFAULT_HEIGHT = 640;
	private String title;
	private DisplayMode currentDisplayMode;
	
	private ArrayList<Scene> scenes = new ArrayList<Scene>();
	protected Scene currentScene = null;
	
	long lastFPS = 0;
	int fps = 0;
	
	protected boolean running = false;
	
	public BasicGame(String title) {
		this.title = title;
	}
	
	@Override
	public abstract void onCreate();
	
	public void addScene(Scene scene) {
		scenes.add(scene);
	}
	
	public void setScene(Scene scene) {
		for(Scene s: scenes) {
			if(s == scene) {
				currentScene = s;
			}
		}
	}
	
	public final void start() throws RadiantException {
		try {
			Display.setTitle(title);
			if(currentDisplayMode == null) {
				currentDisplayMode = new DisplayMode(DEFAULT_WIDTH, DEFAULT_HEIGHT);
			}
			Display.setDisplayMode(currentDisplayMode);
			Display.create();
		} catch(LWJGLException e) {
			throw new RadiantException(e.getMessage());
		}
		
		onCreate();
		lastFPS = getTime(); //set lastFPS to current Time
		running = true;
		update();
	}
	
	@Override
	public void update() {
		while(!Display.isCloseRequested()) {
			if(currentScene != null) {
				currentScene.update();
			}
			updateFPS();
			Display.update();
		}
		Display.destroy();
	}
	
	/**
	 * Get the time in milliseconds
	 * 
	 * @return The system time in milliseconds
	 */
	public long getTime() {
	    return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
	    if (getTime() - lastFPS > 1000) {
	        Display.setTitle("FPS: " + fps); 
	        fps = 0; //reset the FPS counter
	        lastFPS += 1000; //add one second
	    }
	    fps++;
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
