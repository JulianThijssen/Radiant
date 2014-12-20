package radiant.engine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT16;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL30.GL_COMPARE_REF_TO_TEXTURE;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import radiant.assets.AssetLoader;
import radiant.assets.material.Material;
import radiant.assets.material.Shading;
import radiant.assets.scene.Scene;
import radiant.assets.shader.Shader;
import radiant.assets.texture.TextureData;
import radiant.engine.components.AttachedTo;
import radiant.engine.components.Camera;
import radiant.engine.components.DirectionalLight;
import radiant.engine.components.Mesh;
import radiant.engine.components.MeshRenderer;
import radiant.engine.components.PointLight;
import radiant.engine.components.Transform;
import radiant.engine.core.diag.Log;
import radiant.engine.core.file.Path;
import radiant.engine.core.math.Matrix4f;
import radiant.engine.core.math.Vector3f;

public class Renderer implements ISystem {
	private Scene scene;
	
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f modelMatrix = new Matrix4f();
	
	private HashMap<Shading, Shader> shaders = new HashMap<Shading, Shader>();
	private HashMap<Shader, List<Entity>> shaderMap = new HashMap<Shader, List<Entity>>();
	
	private Vector3f clearColor = new Vector3f(0, 0, 0);
	
	private int shadowBuffer = 0;
	
	@Override
	public void create() {
		setGlParameters();
		loadShaders();
	}
	
	@Override
	public void destroy() {
		
	}
	
	/**
	 * Sets the basic OpenGL parameters concerning back face culling,
	 * texture wrapping and alpha handling.
	 */
	private void setGlParameters() {
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND);
		glEnable(GL_BLEND);
		//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		//glBlendFunc(GL_ONE, GL_ONE);
		
		glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
		
		shadowBuffer = glGenFramebuffers();
	}
	
	/**
	 * Initialise all the shader buckets
	 */
	private void loadShaders() {
		shaders.put(Shading.NONE, null);
		shaders.put(Shading.UNSHADED, AssetLoader.loadShader(new Path("shaders/unshaded")));
		shaders.put(Shading.DIFFUSE, AssetLoader.loadShader(new Path("shaders/diffuse")));
		shaders.put(Shading.NORMAL, AssetLoader.loadShader(new Path("shaders/normal")));
		shaders.put(Shading.SPECULAR, AssetLoader.loadShader(new Path("shaders/specular")));
		shaders.put(Shading.SHADOW, AssetLoader.loadShader(new Path("shaders/shadow")));
		
		for(Shader shader: shaders.values()) {
			shaderMap.put(shader, new ArrayList<Entity>());
		}
	}

	/**
	 * Sets the scene that needs to be rendered
	 * @param scene The scene to be rendered
	 */
	public void setScene(Scene scene) {
		this.scene = scene;
	}
	
	/**
	 * Renders the current complete scene graph
	 */
	@Override
	public void update() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		if(scene.mainCamera == null) {
			return;
		}
		
		Camera camera = (Camera) scene.mainCamera.getComponent(Camera.class);
		Transform ct = (Transform) scene.mainCamera.getComponent(Transform.class);
		glEnable(GL_BLEND);
		glBlendFuncSeparate(GL_ONE, GL_ONE, GL_ONE, GL_ONE);
		renderScene(ct, camera);
		glDisable(GL_BLEND);
		//glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void renderScene(Transform transform, Camera camera) {
		glViewport(0, 0, Window.width, Window.height);

		camera.loadProjectionMatrix(projectionMatrix);
		
		// Calculate view matrix
		viewMatrix.setIdentity();
		viewMatrix.rotate(Vector3f.negate(transform.rotation));
		viewMatrix.translate(Vector3f.negate(transform.position));
		
		// Calculate shadow info
		for (DirectionalLight light: scene.dirLights) {
			Transform lightT = (Transform) light.owner.getComponent(Transform.class);
			Camera lightC = new Camera(-10, 10, -10, 10, -10, 20);
			
			if (light.shadowInfo != null) {
				loadShadowInfo(light.shadowInfo, lightT, lightC);
			}
		}
		for (PointLight light: scene.pointLights) {
			Transform lightT = (Transform) light.owner.getComponent(Transform.class);
			Camera lightC = new Camera(90, 1, 0.1f, 20);
			//Camera lightC = new Camera(-10, 10, -10, 10, 0.1f, 20);
			
			for (int i = 0; i < 6; i++) {
				if (light.shadowInfo[i] != null) {
					lightT.rotation = PointLight.shadowTransforms[i];
					loadShadowInfo(light.shadowInfo[i], lightT, lightC);
				}
			}
		}

		// Divide entities into light buckets
		List<Entity> pointLights = scene.getPointLights();
		List<Entity> dirLights = scene.getDirectionalLights();
		divideMeshes();

		// Render all the meshes associated with a shader
		Shader shader = shaders.get(Shading.UNSHADED);
		glUseProgram(shader.handle);
		
		for(Entity entity: shaderMap.get(shader)) {
			drawMesh(shader, entity);
		}
		

		
		shader = shaders.get(Shading.NORMAL);
		glUseProgram(shader.handle);
		
		for(Entity entity: shaderMap.get(shader)) {
			//uploadLights(shader, pointLights, dirLights);
			drawMesh(shader, entity);
		}

		shader = shaders.get(Shading.SPECULAR);
		glUseProgram(shader.handle);
		
		Matrix4f biasMatrix = new Matrix4f();
		biasMatrix.array[0] = 0.5f;
		biasMatrix.array[5] = 0.5f;
		biasMatrix.array[10] = 0.5f;
		biasMatrix.array[12] = 0.5f;
		biasMatrix.array[13] = 0.5f;
		biasMatrix.array[14] = 0.5f;
		glUniformMatrix4(shader.biasMatrixLoc, false, biasMatrix.getBuffer());
		
		for (PointLight light: scene.pointLights) {
			for (int j = 0; j < 6; j++) {
				glClear(GL_DEPTH_BUFFER_BIT);
				Entity e = light.owner;
				Transform lightT = (Transform) e.getComponent(Transform.class);
				
				glActiveTexture(GL_TEXTURE3);
				ShadowInfo shadowInfo = light.shadowInfo[j];
				glBindTexture(GL_TEXTURE_2D, shadowInfo.shadowMap);
				glUniform1i(shader.siMapLoc, 3);
				glUniformMatrix4(shader.siProjectionLoc, false, shadowInfo.projectionMatrix.getBuffer());
				glUniformMatrix4(shader.siViewLoc, false, shadowInfo.viewMatrix.getBuffer());
				//System.out.println("Proj: " + shadowInfo.projectionMatrix.toString());
	
				glUniform1i(shader.isPointLightLoc, 1);
				glUniform1i(shader.isDirLightLoc, 0);
				glUniform3f(shader.plPositionLoc, lightT.position.x, lightT.position.y, lightT.position.z);
				glUniform3f(shader.plColorLoc, light.color.x, light.color.y, light.color.z);
				glUniform1f(shader.plEnergyLoc, light.energy);
				glUniform1f(shader.plDistanceLoc, light.distance);
				
				Transform camT = (Transform) scene.mainCamera.getComponent(Transform.class);
				glUniform3f(shader.cameraPositionLoc, camT.position.x, camT.position.y, camT.position.z);
				
				for(Entity entity: shaderMap.get(shader)) {				
					drawMesh(shader, entity);
				}
			}
		}
		
		for (DirectionalLight light: scene.dirLights) {
			glClear(GL_DEPTH_BUFFER_BIT);
			Entity e = light.owner;
			Transform lightT = (Transform) e.getComponent(Transform.class);
			
			Matrix4f m = new Matrix4f();
			m.rotate(lightT.rotation);
			Vector3f dir = new Vector3f(0, 0, -1);
			dir = m.transform(dir, 0);
			
			glActiveTexture(GL_TEXTURE3);
			ShadowInfo shadowInfo = light.shadowInfo;
			glBindTexture(GL_TEXTURE_2D, shadowInfo.shadowMap);
			glUniform1i(shader.siMapLoc, 3);
			glUniformMatrix4(shader.siProjectionLoc, false, shadowInfo.projectionMatrix.getBuffer());
			glUniformMatrix4(shader.siViewLoc, false, shadowInfo.viewMatrix.getBuffer());
			
			glUniform1i(shader.isDirLightLoc, 1);
			glUniform3f(shader.dlDirectionLoc, dir.x, dir.y, dir.z);
			glUniform3f(shader.dlColorLoc, light.color.x, light.color.y, light.color.z);
			glUniform1f(shader.dlEnergyLoc, light.energy);
			
			Transform camT = (Transform) scene.mainCamera.getComponent(Transform.class);
			glUniform3f(shader.cameraPositionLoc, camT.position.x, camT.position.y, camT.position.z);
			
			for(Entity entity: shaderMap.get(shader)) {				
				drawMesh(shader, entity);
			}
		}
		
		shader = shaders.get(Shading.DIFFUSE);
		glUseProgram(shader.handle);
		
		glActiveTexture(GL_TEXTURE3);
		ShadowInfo shadowInfo = scene.pointLights.get(0).shadowInfo[3];
		//ShadowInfo shadowInfo = scene.dirLights.get(0).shadowInfo;
		glBindTexture(GL_TEXTURE_2D, shadowInfo.shadowMap);
		glUniform1i(shader.siMapLoc, 3);
		glUniformMatrix4(shader.siProjectionLoc, false, shadowInfo.projectionMatrix.getBuffer());
		glUniformMatrix4(shader.siViewLoc, false, shadowInfo.viewMatrix.getBuffer());
		
		for(Entity entity: shaderMap.get(shader)) {
			drawMesh(shader, entity);
		}
	}
	
	private void loadShadowInfo(ShadowInfo shadowInfo, Transform transform, Camera camera) {
		// Render the scene for the shadow map
		Shader shader = shaders.get(Shading.SHADOW);
		glUseProgram(shader.handle);
		
		glBindTexture(GL_TEXTURE_2D, shadowInfo.shadowMap);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, Window.width, Window.height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (FloatBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);//FIXME CLAMP_TO_EDGE
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer);
		glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowInfo.shadowMap, 0);
		glReadBuffer(GL_NONE);
		glDrawBuffer(GL_NONE);
		
		glClear(GL_DEPTH_BUFFER_BIT);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			Log.debug("The framebuffer is not happy");
			int error = glCheckFramebufferStatus(GL_FRAMEBUFFER);
			
			if(error == GL_FRAMEBUFFER_UNDEFINED) { System.out.println("UNDEFINED"); }
			if(error == GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) { System.out.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT"); }
			if(error == GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) { System.out.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT"); }
			if(error == GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER) { System.out.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER"); }
			if(error == GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER) { System.out.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER"); }
			if(error == GL_FRAMEBUFFER_UNSUPPORTED) { System.out.println("GL_FRAMEBUFFER_UNSUPPORTED"); }
			if(error == GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE) { System.out.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE"); }
			if(error == GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE) { System.out.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE"); }
		}
		
		camera.loadProjectionMatrix(shadowInfo.projectionMatrix);
		shadowInfo.viewMatrix.setIdentity();
		shadowInfo.viewMatrix.rotate(Vector3f.negate(transform.rotation));
		shadowInfo.viewMatrix.translate(Vector3f.negate(transform.position));
		
		glUniformMatrix4(shader.siProjectionLoc, false, shadowInfo.projectionMatrix.getBuffer());
		glUniformMatrix4(shader.siViewLoc, false, shadowInfo.viewMatrix.getBuffer());
		
		glDisable(GL_CULL_FACE);
		
		for(Entity entity: scene.getEntities()) {
			Mesh mesh = (Mesh) entity.getComponent(Mesh.class);
			
			if(mesh != null) {
				drawMesh(shader, entity);
			}
		}
		
		glEnable(GL_CULL_FACE);
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	/**
	 * Divide the meshes in the scene into their appropriate shader buckets
	 */
	private void divideMeshes() {
		// Divide entities into shader buckets
		for(List<Entity> meshes: shaderMap.values()) {
			meshes.clear();
		}
		for(Entity e: scene.getEntities()) {
			Mesh mesh = (Mesh) e.getComponent(Mesh.class);
			MeshRenderer mr = (MeshRenderer) e.getComponent(MeshRenderer.class);
			if(mesh == null || mr == null) {
				continue;
			}
			
			Shader shader = shaders.get(mr.material.shading);
			shaderMap.get(shader).add(e);
		}
	}
	
//	private void uploadLights(Shader shader, List<Entity> pointLights, List<Entity> dirLights) {
//		//uploadPointLights(shader, pointLights);
//		uploadDirectionalLights(shader, dirLights);
//	}

	/**
	 * Uploads all the point lights in the scene to the shaders
	 * @param shader The shader currently in use
	 * @param lights The list of entities that have a point light component
	 */
//	private void uploadPointLights(Shader shader, List<Entity> pointLights) {
//		glUniform1i(shader.numPointLightsLoc, pointLights.size());
//		
//		for(int i = 0; i < pointLights.size(); i++) {
//			Entity e = pointLights.get(i);
//			Transform  transform = (Transform) e.getComponent(Transform.class);
//			PointLight light = (PointLight) e.getComponent(PointLight.class);
//
//			glUniform3f(shader.plPositionLocs[i], transform.position.x, transform.position.y, transform.position.z);
//			glUniform3f(shader.plColorLocs[i], light.color.x, light.color.y, light.color.z);
//			glUniform1f(shader.plEnergyLocs[i], light.energy);
//			glUniform1f(shader.plDistanceLocs[i], light.distance);
//			
//			for (int j = 0; j < 6; j++) {
//				int index = i * 6 + j;
//				glActiveTexture(GL_TEXTURE3);
//				glBindTexture(GL_TEXTURE_2D, light.shadowInfo[j].shadowMap);
//				glUniform1i(shader.plShadowInfoMap[index], 3);
//				glUniformMatrix4(shader.plShadowInfoProj[index], false, light.shadowInfo[j].projectionMatrix.getBuffer());
//				glUniformMatrix4(shader.plShadowInfoView[index], false, light.shadowInfo[j].viewMatrix.getBuffer());
//				glBindTexture(GL_TEXTURE_2D, 0);
//			}
//		}
//	}
	
	/**
	 * Uploads all the directional lights in the scene to the shaders
	 * @param shader The shader currently in use
	 * @param lights The list of entities that have a directional light component
	 */
//	private void uploadDirectionalLights(Shader shader, List<Entity> dirLights) {
//		glUniform1i(shader.numDirLightsLoc, dirLights.size());
//		
//		for(int i = 0; i < dirLights.size(); i++) {
//			Entity e = dirLights.get(i);
//			Transform  transform = (Transform) e.getComponent(Transform.class);
//			DirectionalLight light = (DirectionalLight) e.getComponent(DirectionalLight.class);
//
//			Matrix4f m = new Matrix4f();
//			m.rotate(transform.rotation);
//			Vector3f dir = new Vector3f(0, 0, -1);
//			dir = m.transform(dir, 0);
//			
//			glUniform3f(shader.dlDirectionLoc, dir.x, dir.y, dir.z);
//			glUniform3f(shader.dlColorLoc, light.color.x, light.color.y, light.color.z);
//			glUniform1f(shader.dlEnergyLoc, light.energy);
//			
//			glActiveTexture(GL_TEXTURE3);
//			glBindTexture(GL_TEXTURE_2D, light.shadowInfo.shadowMap);
//			glUniform1i(shader.dlShadowInfoMap[i], 3);
//			glUniformMatrix4(shader.dlShadowInfoProj[i], false, light.shadowInfo.projectionMatrix.getBuffer());
//			glUniformMatrix4(shader.dlShadowInfoView[i], false, light.shadowInfo.viewMatrix.getBuffer());
//			//glBindTexture(GL_TEXTURE_2D, 0);
//		}
//	}
	
	/**
	 * Uploads the specified material to the shaders
	 * @param shader The shader currently in use
	 * @param mat    The material to be uploaded
	 */
	private void uploadMaterial(Shader shader, Material mat) {
		// Colors
		glUniform3f(shader.diffuseColorLoc,	mat.diffuseColor.x, mat.diffuseColor.y, mat.diffuseColor.z);
		glUniform3f(shader.specularColorLoc, mat.specularColor.x, mat.specularColor.y, mat.specularColor.z);
		
		glUniform1f(shader.specularIntensityLoc, mat.specularIntensity);
		glUniform2f(shader.tilingLoc, mat.tiling.x, mat.tiling.y);
		glUniform1f(shader.hardnessLoc, mat.hardness);
		
		if(mat.receiveShadows) {
			glUniform1i(shader.receiveShadowsLoc, 1);
		} else {
			glUniform1i(shader.receiveShadowsLoc, 0);
		}
		
		// Diffuse texture
//		if(mat.diffuseMap != null) {
//			TextureData diffuseMap = AssetLoader.loadTexture(mat.diffuseMap);
//
//			glActiveTexture(GL_TEXTURE0);
//			glBindTexture(GL_TEXTURE_2D, diffuseMap.handle);
//			glUniform1i(shader.diffuseMapLoc, 0);
//
//			// Let the shader know we uploaded a diffuse map
//			glUniform1i(shader.hasDiffuseMapLoc, 1);
//		} else {
//			glUniform1i(shader.hasDiffuseMapLoc, 0);
//		}
		// Normal texture
		if(mat.normalMap != null) {
			TextureData normalMap = AssetLoader.loadTexture(mat.normalMap);

			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, normalMap.handle);
			glUniform1i(shader.normalMapLoc, 1);
			
			// Let the shader know we uploaded a normal map
			glUniform1i(shader.hasNormalMapLoc, 1);
		} else {
			glUniform1i(shader.hasNormalMapLoc, 0);
		}
		// Specular texture
		if(mat.specularMap != null) {
			TextureData specularMap = AssetLoader.loadTexture(mat.specularMap);

			glActiveTexture(GL_TEXTURE2);
			glBindTexture(GL_TEXTURE_2D, specularMap.handle);
			glUniform1i(shader.specularMapLoc, 2);

			// Let the shader know we uploaded a specular map
			glUniform1i(shader.hasSpecularMapLoc, 1);
		} else {
			glUniform1i(shader.hasSpecularMapLoc, 0);
		}
	}
	
	/**
	 * Draws the mesh associated with the given entity
	 * @param shader The shader currently in use
	 * @param entity The entity that has the mesh component to be drawn
	 */
	private void drawMesh(Shader shader, Entity entity) {		
		Transform transform = (Transform) entity.getComponent(Transform.class);
		Mesh mesh = (Mesh) entity.getComponent(Mesh.class);
		MeshRenderer mr = (MeshRenderer) entity.getComponent(MeshRenderer.class);
		AttachedTo attached = (AttachedTo) entity.getComponent(AttachedTo.class);
		
		if(transform == null) {
			return;
		}

		modelMatrix.setIdentity();
		
		// Go up the hierarchy and stack transformations if this entity has a parent
		if(attached != null) {
			Entity parent = attached.parent;
			Transform parentT = (Transform) parent.getComponent(Transform.class);
			
			modelMatrix.translate(parentT.position);
			modelMatrix.rotate(parentT.rotation);
			modelMatrix.scale(parentT.scale);
		}
		
		modelMatrix.translate(transform.position);
		modelMatrix.rotate(transform.rotation);
		modelMatrix.scale(transform.scale);
		
		// Upload matrices to the shader
		glUniformMatrix4(shader.projectionMatrixLoc, false, projectionMatrix.getBuffer());
		glUniformMatrix4(shader.viewMatrixLoc, false, viewMatrix.getBuffer());
		glUniformMatrix4(shader.modelMatrixLoc, false, modelMatrix.getBuffer());
		
		if(mr.material != null) {
			uploadMaterial(shader, mr.material);
		}
		
		glBindVertexArray(mesh.handle);
		glDrawArrays(GL_TRIANGLES, 0, mesh.getNumFaces() * 3);
		glBindVertexArray(0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, 0);
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, 0);
		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, 0);
		//glActiveTexture(GL_TEXTURE3);
		//glBindTexture(GL_TEXTURE_2D, 0);
	}
}
