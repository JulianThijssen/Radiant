package com.radiant.components;

import com.radiant.entities.Entity;

public abstract class Component {
	public Entity parent;
	
	public Component(String name) {
		this.name = name;
	}
}
