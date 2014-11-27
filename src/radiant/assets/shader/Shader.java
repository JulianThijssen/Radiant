package radiant.assets.shader;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Shader {
	public static final int MAX_POINT_LIGHTS = 80;
	public static final int MAX_DIRECTIONAL_LIGHTS = 10;
	
	public int handle;
	
	// Locations
	public int projectionMatrixLoc;
	public int viewMatrixLoc;
	public int modelMatrixLoc;
	
	public int[] plPositionLocs    = new int[MAX_POINT_LIGHTS];
	public int[] plAttenuationLocs = new int[MAX_POINT_LIGHTS];
	public int[] plColorLocs       = new int[MAX_POINT_LIGHTS];
	
	public int[] dlColorLocs     = new int[MAX_DIRECTIONAL_LIGHTS];
	public int[] dlDirectionLocs = new int[MAX_DIRECTIONAL_LIGHTS];
	
	
	public int numPointLightsLoc;
	public int numDirLightsLoc;
	
	public int diffuseColorLoc;
	public int tilingLoc;
	
	public int diffuseMapLoc;
	public int normalMapLoc;
	public int specularMapLoc;
	
	public int hasDiffuseMapLoc;
	public int hasNormalMapLoc;
	public int hasSpecularMapLoc;
	
	
	public int cameraPositionLoc;
	
	public Shader(int handle) {
		this.handle = handle;
		
		glUseProgram(handle);
			loadUniformLocations();
		glUseProgram(0);
	}
	
	// Load all uniform locations
	public void loadUniformLocations() {
		// Matrices
		projectionMatrixLoc = glGetUniformLocation(handle, "projectionMatrix");
		viewMatrixLoc = glGetUniformLocation(handle, "viewMatrix");
		modelMatrixLoc = glGetUniformLocation(handle, "modelMatrix");
		
		// Point lights
		for(int i = 0; i < MAX_POINT_LIGHTS; i++) {
			plPositionLocs[i]    = glGetUniformLocation(handle, "pointLights["+i+"].position");
			plAttenuationLocs[i] = glGetUniformLocation(handle, "pointLights["+i+"].attenuation");
			plColorLocs[i]       = glGetUniformLocation(handle, "pointLights["+i+"].color");
		}
		
		// Directional lights
		for(int i = 0; i < MAX_DIRECTIONAL_LIGHTS; i++) {
			dlDirectionLocs[i] = glGetUniformLocation(handle, "dirLights["+i+"].direction");
			dlColorLocs[i]     = glGetUniformLocation(handle, "dirLights["+i+"].color");
		}

		numPointLightsLoc = glGetUniformLocation(handle, "numPointLights");
		numDirLightsLoc   = glGetUniformLocation(handle, "numDirLights");

		// Material
		diffuseColorLoc = glGetUniformLocation(handle, "diffuseColor");
		tilingLoc = glGetUniformLocation(handle, "tiling");
		
		diffuseMapLoc = glGetUniformLocation(handle, "diffuseMap");
		normalMapLoc = glGetUniformLocation(handle, "normalMap");
		specularMapLoc = glGetUniformLocation(handle, "specularMap");
		
		hasDiffuseMapLoc = glGetUniformLocation(handle, "hasDiffuseMap");
		hasNormalMapLoc = glGetUniformLocation(handle, "hasNormalMap");
		hasSpecularMapLoc = glGetUniformLocation(handle, "hasSpecularMap");
		
		cameraPositionLoc = glGetUniformLocation(handle, "camera_position");
	}
}
