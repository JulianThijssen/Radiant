package com.radiant;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import com.radiant.entities.Entity;

public class Scene {
	public CopyOnWriteArrayList<Entity> entities = new CopyOnWriteArrayList<Entity>();
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
		entities.add(c);
	}
	
	public void update() {
		for(Script s: scripts) {
			s.update(this);
		}
	}
}
