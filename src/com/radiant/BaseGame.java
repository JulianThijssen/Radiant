package com.radiant;

import java.util.ArrayList;

import org.lwjgl.Sys;

import com.radiant.assets.AssetLoader;
import com.radiant.exceptions.RadiantException;
import com.radiant.util.Log;

public abstract class BaseGame {
	/* System */
	private Window window;
	private Renderer renderer;
	
	/* Scene */
	private ArrayList<Scene> scenes = new ArrayList<Scene>();
	protected Scene currentScene = null;
	
	/* Game loop */
	public static final int MAX_SKIP = 15;
	public static final int SKIP_TIME = 40;
	
	/* FPS */
	public int fps = 0;
	
	public final void startGame() throws RadiantException {
		if(AssetLoader.getErrors() > 0) {
			throw new RadiantException("Can't start game, there are unresolved errors");
		}
		window = Window.create();
		renderer = new Renderer();
		
		update();
	}
	
	public void shutdown() {
		window.destroy();
	}
	
	public void update() {
		long nextUpdate = System.currentTimeMillis();
		long lastFpsCount = System.currentTimeMillis();
		
		int frames = 0;
		
		if(currentScene != null) {
			currentScene.start();
		
			while(!window.isClosed()) {
				int skipped = 0;
				
				//Count the FPS
			    if (System.currentTimeMillis() - lastFpsCount > 1000) {
			      lastFpsCount = System.currentTimeMillis();
			      fps = frames;
			      Log.debug("FPS: " + fps);
			      frames = 0;
			    }
				
				while(System.currentTimeMillis() > nextUpdate && skipped < MAX_SKIP) {
					if(currentScene != null) {
						currentScene.update();
					}
					nextUpdate += SKIP_TIME;
					skipped++;
				}
				renderer.update(currentScene, 0);
				window.update();
				frames++;
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
}
