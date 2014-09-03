package com.radiant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.radiant.entities.Entity;

public class Scene {
	private CopyOnWriteArrayList<Entity> entities = new CopyOnWriteArrayList<Entity>();
	private ArrayList<Script> scripts = new ArrayList<Script>();
	public Entity mainCamera;
	
	public void addEntity(Entity e) {
		entities.add(e);
	}
	
	public void addScript(Script s) {
		scripts.add(s);
	}
	
	public void addCamera(Entity c) {
		mainCamera = c;
		entities.add(c);
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	public List<Script> getScripts() {
		return scripts;
	}
	
	public void start() {
		for(Script s: scripts) {
			s.onStart(this);
		}
	}
	
	public void update() {
		for(Script s: scripts) {
			s.update(this);
		}
	}
}
