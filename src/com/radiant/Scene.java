package com.radiant;

import java.util.ArrayList;

import com.radiant.entities.Entity;

public class Scene {
	public ArrayList<Entity> entities = new ArrayList<Entity>();
	public ArrayList<Script> scripts = new ArrayList<Script>();
	public Entity mainCamera;
	
	public void addEntity(Entity e) {
		entities.add(e);
	}
	
	public void addScript(Script s) {
		scripts.add(s);
		s.onStart();
	}
	
	public void addCamera(Entity c) {
		mainCamera = c;
	}
	
	public void update() {
		for(Script s: scripts) {
			for(Entity e: entities) {
				s.update(e);
			}
			s.update(mainCamera);
		}
	}
}
