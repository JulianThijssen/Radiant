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
	public RenderManager renderer;
	
	/* FPS */
	long lastFPS = 0;
	int fps = 0;
	
	
	/*
	 * Engine
	 */
	public void startup() throws RadiantException {
		window = Window.create();
		
		renderer = new RenderManager(this);
		renderer.create();
	}
	
	public void shutdown() {
		window.destroy();
	}
	
	public void update() {
		while(!window.isClosed()) {
			long t = System.nanoTime();
			if(currentScene != null) {
				currentScene.update();
			}
			renderer.render();
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
	public final void startGame() {
		if(AssetManager.getErrors() > 0) {
			Log.debug("Can't start game, there are unresolved errors");
			return;
		}
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
