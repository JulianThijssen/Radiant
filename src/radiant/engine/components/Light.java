package radiant.engine.components;

import radiant.engine.core.math.Vector3f;

public class Light extends Component {
	public Vector3f color = new Vector3f(1, 1, 1);
	public float constantAtt = 0.1f;
	public float linearAtt = 0.25f;
	public float quadraticAtt = 0.005f;
}
