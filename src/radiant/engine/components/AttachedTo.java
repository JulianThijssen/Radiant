package radiant.engine.components;

import radiant.engine.Entity;

public class AttachedTo extends Component {
	public Entity parent;
	
	public AttachedTo(Entity parent) {
		this.parent = parent;
	}
}
