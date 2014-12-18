package radiant.engine.components;

import static org.lwjgl.opengl.GL11.glGenTextures;
import radiant.engine.ShadowInfo;
import radiant.engine.core.math.Matrix4f;
import radiant.engine.core.math.Vector3f;

public class PointLight extends Component {
	public static final Vector3f[] shadowTransforms = {
		new Vector3f(0, 0, 0),   // Forward
		new Vector3f(0, 90, 0),  // Left
		new Vector3f(0, 180, 0), // Backward
		new Vector3f(0, -90, 0), // Right
		new Vector3f(90, 0, 0),  // Up
		new Vector3f(-90, 0, 0)  // Down
	};
	
	public Vector3f color = new Vector3f(1, 1, 1);
	
	public float energy = 1;
	public float distance = 1;
	
	public ShadowInfo[] shadowInfo = null;
	
	public PointLight() {
		shadowInfo = new ShadowInfo[6];
		
		for (int i = 0; i < 6; i++) {
			int shadowMap = glGenTextures();
			shadowInfo[i] = new ShadowInfo(shadowMap, new Matrix4f(), new Matrix4f());
		}
	}
}
