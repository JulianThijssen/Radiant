package com.radiant;

import java.util.ArrayList;

import com.radiant.entities.Camera;
import com.radiant.entities.Entity;
import com.radiant.entities.Light;

public class Scene {
	public Camera mainCamera;
	public ArrayList<Light> lights = new ArrayList<Light>();
	public ArrayList<Entity> entities = new ArrayList<Entity>();
	
	public void addEntity(Entity e) {
		entities.add(e);
	}
	
	public void update() {
		for(Entity e: entities) {
			e.update(this);
		}
	}
}
