package com.radiant.entities;

import com.radiant.Scene;
import com.radiant.components.Transform;

public abstract class Entity {
	public String name;
	
	public Transform transform = new Transform();
	public Mesh mesh = null;
	
	public abstract void create();
	
	public abstract void update(Scene world);
	
	public abstract void destroy();
}
