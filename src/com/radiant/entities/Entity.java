package com.radiant.entities;

import java.util.ArrayList;

import com.radiant.components.Component;

public class Entity {
	public String name;
	public ArrayList<Component> components = new ArrayList<Component>();
	
	public void addComponent(Component c) {
		components.add(c);
	}
	
	public Component getComponent(String name) {
		for(Component c: components) {
			if(c.name.equals(name)) {
				return c;
			}
		}
		return null;
	}
}
