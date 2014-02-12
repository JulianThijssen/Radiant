package com.radiant.entities;

import java.util.ArrayList;

import com.radiant.components.Component;

public class Entity {
	public String name;
	public ArrayList<Component> components = new ArrayList<Component>();
	
	public void addComponent(Component c) {
		components.add(c);
	}
}
