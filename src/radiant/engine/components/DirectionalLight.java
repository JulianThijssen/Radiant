package radiant.engine.components;

import radiant.engine.core.math.Vector3f;

public class DirectionalLight extends Component {
	public Vector3f color = new Vector3f(1, 1, 1);
	
	public float energy;
}
