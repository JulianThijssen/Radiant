package com.radiant;

import java.util.ArrayList;

import com.radiant.entities.Entity;

public class Scene {
	public ArrayList<Entity> entities = new ArrayList<Entity>();
	public Camera mainCamera = new Camera();
	
	public void addEntity(Entity e) {
		entities.add(e);
	}
	
	public void update() {
		
	}
}
