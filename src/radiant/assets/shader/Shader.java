package radiant.assets.shader;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Shader {
	public static final int MAX_POINT_LIGHTS = 20;
	public static final int MAX_DIRECTIONAL_LIGHTS = 10;
	
	public int handle;
	
	// Locations
	public int projectionMatrixLoc;
	public int viewMatrixLoc;
	public int modelMatrixLoc;
	
	public int siMapLoc;
	public int siProjectionLoc;
	public int siViewLoc;
	
	public int[] plPositionLocs     = new int[MAX_POINT_LIGHTS];
	public int[] plEnergyLocs       = new int[MAX_POINT_LIGHTS];
	public int[] plDistanceLocs     = new int[MAX_POINT_LIGHTS];
	public int[] plColorLocs        = new int[MAX_POINT_LIGHTS];
	public int[] plShadowInfoMap  = new int[MAX_POINT_LIGHTS * 6];
	public int[] plShadowInfoProj = new int[MAX_POINT_LIGHTS * 6];
	public int[] plShadowInfoView = new int[MAX_POINT_LIGHTS * 6];
	
	public int[] dlColorLocs     = new int[MAX_DIRECTIONAL_LIGHTS];
	public int[] dlDirectionLocs = new int[MAX_DIRECTIONAL_LIGHTS];
	public int[] dlEnergyLocs    = new int[MAX_DIRECTIONAL_LIGHTS];
	
	public int[] dlShadowInfoMap  = new int[MAX_DIRECTIONAL_LIGHTS];
	public int[] dlShadowInfoProj = new int[MAX_DIRECTIONAL_LIGHTS];
	public int[] dlShadowInfoView = new int[MAX_DIRECTIONAL_LIGHTS];
	
	public int numPointLightsLoc;
	public int numDirLightsLoc;
	
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
		
		
		siMapLoc = glGetUniformLocation(handle, "shadowInfo.shadowMap");
		siProjectionLoc = glGetUniformLocation(handle, "shadowInfo.projectionMatrix");
		siViewLoc = glGetUniformLocation(handle, "shadowInfo.viewMatrix");
		
		// Point lights
		for(int i = 0; i < MAX_POINT_LIGHTS; i++) {
			plPositionLocs[i]    = glGetUniformLocation(handle, "pointLights["+i+"].position");
			plEnergyLocs[i]      = glGetUniformLocation(handle, "pointLights["+i+"].energy");
			plDistanceLocs[i]    = glGetUniformLocation(handle, "pointLights["+i+"].distance");
			plColorLocs[i]       = glGetUniformLocation(handle, "pointLights["+i+"].color");
			
			for (int j = 0; j < 6; j++) {
				int index = i * 6 + j;
				plShadowInfoMap[index]  = glGetUniformLocation(handle, "plShadowInfo["+index+"].shadowMap");
				plShadowInfoProj[index] = glGetUniformLocation(handle, "plShadowInfo["+index+"].projectionMatrix");
				plShadowInfoView[index] = glGetUniformLocation(handle, "plShadowInfo["+index+"].viewMatrix");
			}
		}
		
		// Directional lights
		for(int i = 0; i < MAX_DIRECTIONAL_LIGHTS; i++) {
			dlDirectionLocs[i] = glGetUniformLocation(handle, "dirLights["+i+"].direction");
			dlColorLocs[i]     = glGetUniformLocation(handle, "dirLights["+i+"].color");
			dlEnergyLocs[i]    = glGetUniformLocation(handle, "dirLights["+i+"].energy");
			
			dlShadowInfoMap[i]  = glGetUniformLocation(handle, "dlShadowInfo["+i+"].shadowMap");
			dlShadowInfoProj[i] = glGetUniformLocation(handle, "dlShadowInfo["+i+"].projectionMatrix");
			dlShadowInfoView[i] = glGetUniformLocation(handle, "dlShadowInfo["+i+"].viewMatrix");
		}

		numPointLightsLoc = glGetUniformLocation(handle, "numPointLights");
		numDirLightsLoc   = glGetUniformLocation(handle, "numDirLights");
		
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
