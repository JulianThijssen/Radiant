package radiant.engine.components;

import radiant.engine.CubeMap;
import radiant.engine.MapType;
import radiant.engine.core.math.Vector3f;

public class PointLight extends Component {
	public static final float DEFAULT_ENERGY = 1.0f;
	public static final float DEFAULT_DISTANCE = 5.0f;
	public static final boolean DEFAULT_CAST_SHADOWS = false;

	public CubeMap shadowMap = new CubeMap(MapType.SHADOW_MAP);
	
	public Vector3f color = new Vector3f(1, 1, 1);
	
	public float energy = DEFAULT_ENERGY;
	public float distance = DEFAULT_DISTANCE;
	public boolean castShadows = DEFAULT_CAST_SHADOWS;
	
	public PointLight() {

	}
}
