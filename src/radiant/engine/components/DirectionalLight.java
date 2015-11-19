package radiant.engine.components;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT16;

import java.nio.FloatBuffer;

import radiant.engine.ShadowInfo;
import radiant.engine.core.math.Vector3f;

public class DirectionalLight extends Component {
	public static final float DEFAULT_ENERGY = 1.0f;
	public static final boolean DEFAULT_CAST_SHADOWS = false;
	public static final float DEFAULT_SHADOW_BIAS = 0.05f;
	
	public Vector3f color = new Vector3f(1, 1, 1);
	
	public float energy = DEFAULT_ENERGY;
	public boolean castShadows = DEFAULT_CAST_SHADOWS;
	public float shadowBias = DEFAULT_SHADOW_BIAS;
	
	public ShadowInfo shadowInfo = null;
	
	public DirectionalLight() {
		// Generate a shadow info object
		int shadowMap = glGenTextures();
		shadowInfo = new ShadowInfo(shadowMap);
		
		glBindTexture(GL_TEXTURE_2D, shadowMap);
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, 1024, 1024, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (FloatBuffer) null);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		glBindTexture(GL_TEXTURE_2D, 0);
	}
}
