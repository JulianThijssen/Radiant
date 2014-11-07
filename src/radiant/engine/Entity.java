package radiant.engine;

import java.util.ArrayList;
import java.util.List;

import radiant.engine.components.Component;

public class Entity {
	public String name;
	private List<Component> components = new ArrayList<Component>();
	
	public void addComponent(Component c) {
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
