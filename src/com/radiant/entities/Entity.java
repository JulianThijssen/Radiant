package com.radiant.entities;

import java.util.ArrayList;

import com.radiant.components.Component;

public class Entity {
	public String name;
	public ArrayList<Component> components = new ArrayList<Component>();
	
	public void addComponent(Component c) {
//		if(getComponent(c.getClass()) != null) {
//			//FIXME make new component already added exception
//			throw new Exception("This component is already added");
//		}
		c.owner = this;
		components.add(c);
	}

	public Component getComponent(Class<?> type) {
		for(Component c: components) {
			if(c.getClass() == type) {
				return c;
			}
		}
		return null;
	}
}
