package radiant.assets.shader;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Shader {
	public static final int MAX_POINT_LIGHTS = 20;
	public static final int MAX_DIRECTIONAL_LIGHTS = 10;
	
	public int handle;
	
	/* Locations of shader uniforms */
	public int projectionMatrixLoc;
	public int viewMatrixLoc;
	public int modelMatrixLoc;
	
	public int biasMatrixLoc;
	
	// Light
	public int siMapLoc;
	public int siLightPosLoc;
	public int siCubeMapLoc;
	public int siProjectionLoc;
	public int siViewLoc;

	public int plPositionLoc;
	public int plColorLoc;
	public int plDistanceLoc;
	public int plEnergyLoc;
	public int plCastShadowsLoc;
	
	public int dlColorLoc;
	public int dlDirectionLoc;
	public int dlEnergyLoc;
	public int dlCastShadowsLoc;
	
	public int isPointLightLoc;
	public int isDirLightLoc;
	
	// Material
	public int materialLoc;
	public int diffuseColorLoc;
	public int specularColorLoc;
	
	public int specularIntensityLoc;
	
	public int tilingLoc;
	public int hardnessLoc;
	
	public int diffuseMapLoc;
	public int normalMapLoc;
	public int specularMapLoc;
	public int reflectionMapLoc;
	
	public int hasDiffuseMapLoc;
	public int hasNormalMapLoc;
	public int hasSpecularMapLoc;
	public int hasReflectionMapLoc;
	
	public int receiveShadowsLoc;
	
	// Camera
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
		
		biasMatrixLoc = glGetUniformLocation(handle, "biasMatrix");
		
		siMapLoc = glGetUniformLocation(handle, "shadowInfo.shadowMap");
		siLightPosLoc = glGetUniformLocation(handle, "lightPos");
		siCubeMapLoc = glGetUniformLocation(handle, "shadowCubeMap");
		siProjectionLoc = glGetUniformLocation(handle, "shadowInfo.projectionMatrix");
		siViewLoc = glGetUniformLocation(handle, "shadowInfo.viewMatrix");
		
		plPositionLoc    = glGetUniformLocation(handle, "pointLight.position");
		plColorLoc       = glGetUniformLocation(handle, "pointLight.color");
		plDistanceLoc    = glGetUniformLocation(handle, "pointLight.distance");
		plEnergyLoc      = glGetUniformLocation(handle, "pointLight.energy");
		plCastShadowsLoc = glGetUniformLocation(handle, "pointLight.castShadows");
		
		dlDirectionLoc   = glGetUniformLocation(handle, "dirLight.direction");
		dlColorLoc       = glGetUniformLocation(handle, "dirLight.color");
		dlEnergyLoc      = glGetUniformLocation(handle, "dirLight.energy");
		dlCastShadowsLoc = glGetUniformLocation(handle, "dirLight.castShadows");

		isPointLightLoc = glGetUniformLocation(handle, "isPointLight");
		isDirLightLoc   = glGetUniformLocation(handle, "isDirLight");
		
		// Material
		materialLoc = glGetUniformLocation(handle, "material");
		diffuseColorLoc = glGetUniformLocation(handle, "material.diffuseColor");
		specularColorLoc = glGetUniformLocation(handle, "material.specularColor");
		specularIntensityLoc = glGetUniformLocation(handle, "material.specularIntensity");
		hardnessLoc = glGetUniformLocation(handle, "material.hardness");
		tilingLoc = glGetUniformLocation(handle, "material.tiling");
		
		//diffuseMapLoc = glGetUniformLocation(handle, "material.diffuseMap");
		normalMapLoc = glGetUniformLocation(handle, "material.normalMap");
		specularMapLoc = glGetUniformLocation(handle, "material.specularMap");
		reflectionMapLoc = glGetUniformLocation(handle, "material.reflectionMap");
		
		hasDiffuseMapLoc = glGetUniformLocation(handle, "material.hasDiffuseMap");
		hasNormalMapLoc = glGetUniformLocation(handle, "material.hasNormalMap");
		hasSpecularMapLoc = glGetUniformLocation(handle, "material.hasSpecularMap");
		hasReflectionMapLoc = glGetUniformLocation(handle, "material.hasReflectionMap");
		
		receiveShadowsLoc = glGetUniformLocation(handle, "material.receiveShadows");
		
		cameraPositionLoc = glGetUniformLocation(handle, "cameraPosition");
	}
}
