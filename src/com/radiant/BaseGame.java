package com.radiant;

import java.util.ArrayList;

import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;

import com.radiant.assets.AssetLoader;
import com.radiant.exceptions.RadiantException;

public abstract class BaseGame {
	/* System */
	private Window window;
	
	/* Scene */
	private ArrayList<Scene> scenes = new ArrayList<Scene>();
	protected Scene currentScene = null;
	
	/* FPS */
	long lastFPS = 0;
	int fps = 0;
	
	public final void startGame() throws RadiantException {
		if(AssetLoader.getErrors() > 0) {
			throw new RadiantException("Can't start game, there are unresolved errors");
		}
		window = Window.create();
		lastFPS = getTime(); //set lastFPS to current Time
		update();
	}
	
	public void shutdown() {
		window.destroy();
	}
	
	public void update() {
		if(currentScene != null) {
			currentScene.start();
		
			while(!window.isClosed()) {
				long t = System.nanoTime();
				if(currentScene != null) {
					currentScene.update();
				}
				window.update();
				updateFPS();
				long dt = System.nanoTime() - t;
				
				long lag = (long) (1000000000/60) - dt;
				long millis = lag / 1000000;
				int nanos = (int) (lag - (millis * 1000000));
				if(lag > 0) {
					try {
						Thread.sleep(millis, nanos);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				Time.deltaTime = (System.nanoTime() - t) / 1000000000f;
			}
		}
		shutdown();
	}
	
	/* Scene */
	public void addScene(Scene scene) {
		scenes.add(scene);
	}
	
	public Scene getScene() {
		return currentScene;
	}
	
	public void setScene(Scene scene) {
		for(Scene s: scenes) {
			if(s == scene) {
				currentScene = s;
			}
		}
	}
	
	/* FPS */
	private long getTime() {
	    return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	private void updateFPS() {
	    if (getTime() - lastFPS > 1000) {
	        Display.setTitle("FPS: " + fps); 
	        fps = 0;
	        lastFPS += 1000;
	    }
	    fps++;
	}
}
