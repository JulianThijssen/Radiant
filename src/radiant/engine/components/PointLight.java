package radiant.engine.components;

import radiant.engine.core.math.Vector3f;

public class PointLight extends Component {
	public Vector3f color = new Vector3f(1, 1, 1);
	public Vector3f attenuation = new Vector3f(0.1f, 0.25f, 0.005f);
}
