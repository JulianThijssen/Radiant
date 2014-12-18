package radiant.engine.components;

import static org.lwjgl.opengl.GL11.glGenTextures;
import radiant.engine.ShadowInfo;
import radiant.engine.core.math.Matrix4f;
import radiant.engine.core.math.Vector3f;

public class DirectionalLight extends Component {
	public Vector3f color = new Vector3f(1, 1, 1);
	
	public float energy = 1.0f;
	
	public ShadowInfo shadowInfo = null;
	
	public DirectionalLight() {
		int shadowMap = glGenTextures();
		shadowInfo = new ShadowInfo(shadowMap, new Matrix4f(), new Matrix4f());
	}
}
