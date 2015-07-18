package radiant.engine.components;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
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
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT16;
import static org.lwjgl.opengl.GL30.GL_R32F;

import java.nio.FloatBuffer;

import radiant.engine.core.math.Vector3f;

public class PointLight extends Component {
	public static final float DEFAULT_ENERGY = 1.0f;
	public static final float DEFAULT_DISTANCE = 5.0f;
	public static final boolean DEFAULT_CAST_SHADOWS = false;
	public static final int DEFAULT_SHADOW_RESOLUTION = 1024;
	
	public static final Vector3f[] shadowTransforms = {
		new Vector3f(180, 90, 0),  // Positive X
		new Vector3f(180, -90, 0), // Negative X
		new Vector3f(90, 0, 0),    // Positive Y
		new Vector3f(-90, 0, 0),   // Negative Y
		new Vector3f(180, 0, 0),   // Positive Z
		new Vector3f(180, 180, 0)  // Negative Z
	};
	
	public Vector3f color = new Vector3f(1, 1, 1);
	
	public float energy = DEFAULT_ENERGY;
	public float distance = DEFAULT_DISTANCE;
	public boolean castShadows = DEFAULT_CAST_SHADOWS;
	public int shadowRes = DEFAULT_SHADOW_RESOLUTION;
	
	public int shadowMap = -1;
	public int depthMap = -1;
	
	public PointLight() {
		// Generate a depth map
		depthMap = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, depthMap);
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, 1024, 1024, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (FloatBuffer) null);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		glBindTexture(GL_TEXTURE_2D, 0);
		
		// Generate a color map to store the shadow map in
		shadowMap = glGenTextures();
		
		glBindTexture(GL_TEXTURE_CUBE_MAP, shadowMap);

		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

		for (int i = 0; i < 6; i++) {
			glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_R32F, shadowRes, shadowRes, 0, GL_RED, GL_FLOAT, (FloatBuffer) null);
		}
		
		glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
	}
}
