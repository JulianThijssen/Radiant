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

	public <T extends Component> T getComponent(Class<T> type) {
		for(Component c: components) {
			if(c.getClass() == type) {
				@SuppressWarnings("unchecked")
				T comp = (T) c; 
				return c;
			}
		}
		return null;
	}
}
