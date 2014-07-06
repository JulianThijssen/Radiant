package com.radiant;

import java.util.ArrayList;

import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;

import com.radiant.assets.AssetLoader;
import com.radiant.exceptions.RadiantException;

public abstract class BaseGame {
	/* System */
	private Window window;
	private Renderer renderer;
	
	/* Scene */
	private ArrayList<Scene> scenes = new ArrayList<Scene>();
	protected Scene currentScene = null;
	
	/* FPS */
	public static final int MAX_SKIP = 15;
	public static final int SKIP_TIME = 40;
	
	long lastFPS = 0;
	int fps = 0;
	
	public final void startGame() throws RadiantException {
		if(AssetLoader.getErrors() > 0) {
			throw new RadiantException("Can't start game, there are unresolved errors");
		}
		window = Window.create();
		renderer = new Renderer();
		lastFPS = getTime(); //set lastFPS to current Time
		update();
	}
	
	public void shutdown() {
		window.destroy();
	}
	
	public void update() {
		long nextUpdate = System.currentTimeMillis();
		
		if(currentScene != null) {
			currentScene.start();
		
			while(!window.isClosed()) {
				int skipped = 0;
				
				
				while(System.currentTimeMillis() > nextUpdate && skipped < MAX_SKIP) {
					if(currentScene != null) {
						currentScene.update();
					}
					nextUpdate += SKIP_TIME;
					skipped++;
				}
				renderer.update(currentScene);
				window.update();
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
