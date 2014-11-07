package radiant.engine.components;

import radiant.engine.core.math.Vector3f;

public class Body extends Component {
	public Vector3f velocity = new Vector3f(0, 0, 0);
	public boolean useGravity = true;
}
