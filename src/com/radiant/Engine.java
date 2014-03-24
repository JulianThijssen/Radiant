package com.radiant;

import java.util.ArrayList;

import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;

import com.radiant.exceptions.RadiantException;
import com.radiant.managers.AssetManager;
import com.radiant.managers.RenderManager;
import com.radiant.util.Log;

public class Engine {
	/* System */
	private Window window;
	
	/* Managers */
	public AssetManager assetManager;
	public RenderManager renderer;
	
	/* FPS */
	long lastFPS = 0;
	int fps = 0;
	
	
	/*
	 * Engine
	 */
	public void startup() throws RadiantException {
		window = Window.create();
		
		assetManager = new AssetManager();
		assetManager.loadAll();
		
		renderer = new RenderManager(this);
		renderer.create();
	}
	
	public void shutdown() {
		window.destroy();
	}
	
	public void update() {
		while(!window.isClosed()) {
			if(currentScene != null) {
				currentScene.update();
			}
			renderer.render();
			window.update();
		}
		shutdown();
	}
	
	/*
	 * Scene 
	 */
	private ArrayList<Scene> scenes = new ArrayList<Scene>();
	protected Scene currentScene = null;
	
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
	
	/*
	 * Game
	 */
	public final void startGame() throws RadiantException {
		lastFPS = getTime(); //set lastFPS to current Time
		update();
	}
	
	public long getTime() {
	    return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
	
	public void updateFPS() {
	    if (getTime() - lastFPS > 1000) {
	        Display.setTitle("FPS: " + fps); 
	        fps = 0;
	        lastFPS += 1000;
	    }
	    fps++;
	}
	
	public static void main(String[] args) {
		try {
			Engine engine = new Engine();
			engine.startup();
		} catch (RadiantException e) {
			Log.info("Failed to start game: " + e.getMessage());
		}
	}
}
