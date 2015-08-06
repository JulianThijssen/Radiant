package radiant.engine.components;

import radiant.engine.CubeMap;
import radiant.engine.core.math.Vector3f;

public class PointLight extends Component {
	public static final float DEFAULT_ENERGY = 1.0f;
	public static final float DEFAULT_DISTANCE = 5.0f;
	public static final boolean DEFAULT_CAST_SHADOWS = false;
	//public static final int DEFAULT_SHADOW_RESOLUTION = 1024;
	
//	public static final Vector3f[] shadowTransforms = {
//		new Vector3f(180, 90, 0),  // Positive X
//		new Vector3f(180, -90, 0), // Negative X
//		new Vector3f(90, 0, 0),    // Positive Y
//		new Vector3f(-90, 0, 0),   // Negative Y
//		new Vector3f(180, 0, 0),   // Positive Z
//		new Vector3f(180, 180, 0)  // Negative Z
//	};
	
	public CubeMap shadowMap = new CubeMap();
	
	public Vector3f color = new Vector3f(1, 1, 1);
	
	public float energy = DEFAULT_ENERGY;
	public float distance = DEFAULT_DISTANCE;
	public boolean castShadows = DEFAULT_CAST_SHADOWS;
	//public int shadowRes = DEFAULT_SHADOW_RESOLUTION;
	
	public PointLight() {
		
	}
}
