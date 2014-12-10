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
	
	public int sprojectionMatrixLoc;
	public int sviewMatrixLoc;
	
	public int[] plPositionLocs    = new int[MAX_POINT_LIGHTS];
	public int[] plAttenuationLocs = new int[MAX_POINT_LIGHTS];
	public int[] plDistanceLocs    = new int[MAX_POINT_LIGHTS];
	public int[] plColorLocs       = new int[MAX_POINT_LIGHTS];
	
	public int[] dlColorLocs     = new int[MAX_DIRECTIONAL_LIGHTS];
	public int[] dlDirectionLocs = new int[MAX_DIRECTIONAL_LIGHTS];
	
	
	public int numPointLightsLoc;
	public int numDirLightsLoc;
	
	public int shadowMapLoc;
	
	public int materialLoc;
	public int diffuseColorLoc;
	public int specularColorLoc;
	
	public int specularIntensityLoc;
	
	public int tilingLoc;
	public int hardnessLoc;
	
	public int diffuseMapLoc;
	public int normalMapLoc;
	public int specularMapLoc;
	
	public int hasDiffuseMapLoc;
	public int hasNormalMapLoc;
	public int hasSpecularMapLoc;
	
	public int receiveShadowsLoc;
	
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
		
		sprojectionMatrixLoc = glGetUniformLocation(handle, "sprojectionMatrix");
		sviewMatrixLoc = glGetUniformLocation(handle, "sviewMatrix");
		
		// Point lights
		for(int i = 0; i < MAX_POINT_LIGHTS; i++) {
			plPositionLocs[i]    = glGetUniformLocation(handle, "pointLights["+i+"].position");
			plAttenuationLocs[i] = glGetUniformLocation(handle, "pointLights["+i+"].attenuation");
			plDistanceLocs[i]    = glGetUniformLocation(handle, "pointLights["+i+"].distance");
			plColorLocs[i]       = glGetUniformLocation(handle, "pointLights["+i+"].color");
		}
		
		// Directional lights
		for(int i = 0; i < MAX_DIRECTIONAL_LIGHTS; i++) {
			dlDirectionLocs[i] = glGetUniformLocation(handle, "dirLights["+i+"].direction");
			dlColorLocs[i]     = glGetUniformLocation(handle, "dirLights["+i+"].color");
		}

		numPointLightsLoc = glGetUniformLocation(handle, "numPointLights");
		numDirLightsLoc   = glGetUniformLocation(handle, "numDirLights");

		shadowMapLoc = glGetUniformLocation(handle, "shadowMap");
		
		// Material
		materialLoc = glGetUniformLocation(handle, "material");
		diffuseColorLoc = glGetUniformLocation(handle, "material.diffuseColor");
		specularColorLoc = glGetUniformLocation(handle, "material.specularColor");
		specularIntensityLoc = glGetUniformLocation(handle, "material.specularIntensity");
		hardnessLoc = glGetUniformLocation(handle, "material.hardness");
		tilingLoc = glGetUniformLocation(handle, "material.tiling");
		
		diffuseMapLoc = glGetUniformLocation(handle, "material.diffuseMap");
		normalMapLoc = glGetUniformLocation(handle, "material.normalMap");
		specularMapLoc = glGetUniformLocation(handle, "material.specularMap");
		
		hasDiffuseMapLoc = glGetUniformLocation(handle, "material.hasDiffuseMap");
		hasNormalMapLoc = glGetUniformLocation(handle, "material.hasNormalMap");
		hasSpecularMapLoc = glGetUniformLocation(handle, "material.hasSpecularMap");
		
		receiveShadowsLoc = glGetUniformLocation(handle, "material.receiveShadows");
		
		cameraPositionLoc = glGetUniformLocation(handle, "cameraPosition");
	}
}
