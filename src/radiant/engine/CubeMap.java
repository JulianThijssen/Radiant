package radiant.engine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT16;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL30.GL_R32F;

import java.nio.FloatBuffer;

import radiant.engine.core.math.Vector3f;

public class CubeMap {
	private static final int DEFAULT_RESOLUTION = 256;
	
	public static final Vector3f[] transforms = {
		new Vector3f(180, 90, 0),  // Positive X
		new Vector3f(180, -90, 0), // Negative X
		new Vector3f(90, 0, 0),    // Positive Y
		new Vector3f(-90, 0, 0),   // Negative Y
		new Vector3f(180, 0, 0),   // Positive Z
		new Vector3f(180, 180, 0)  // Negative Z
	};
	
	private MapType type;
	private int resolution = DEFAULT_RESOLUTION;
	
	public int colorMap = -1;
	public int depthMap = -1;
	
	public CubeMap(MapType type) {
		this.type = type;
		reset();
	}
	
	public int getResolution() {
		return resolution;
	}
	
	public void setResolution(int resolution) {
		this.resolution = resolution;
		reset();
	}
	
	public void reset() {
		if (type == MapType.REFLECTION_MAP) {
			// Generate a reflection map
			depthMap = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, depthMap);
			
			glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, resolution, resolution, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (FloatBuffer) null);
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	
			glBindTexture(GL_TEXTURE_2D, 0);
			
			// Generate a color map to store the shadow map in
			colorMap = glGenTextures();
			
			glBindTexture(GL_TEXTURE_CUBE_MAP, colorMap);
	
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
	
			for (int i = 0; i < 6; i++) {
				glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA8, resolution, resolution, 0, GL_RGBA, GL_UNSIGNED_BYTE, (FloatBuffer) null);
			}
			
			glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
		}
		if (type == MapType.SHADOW_MAP) {		
			// Generate a color map to store the shadow map in
			depthMap = glGenTextures();
			
			glBindTexture(GL_TEXTURE_CUBE_MAP, depthMap);
	
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
	
			for (int i = 0; i < 6; i++) {
				glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_DEPTH_COMPONENT24, resolution, resolution, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (FloatBuffer) null);
			}
			
			glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
		}
	}
}
