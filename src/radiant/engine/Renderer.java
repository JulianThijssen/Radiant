package radiant.engine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT16;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_FUNC;
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
	
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f modelMatrix = new Matrix4f();
	
	private Matrix4f sprojectionMatrix;
	private Matrix4f sviewMatrix = new Matrix4f();
	
	private HashMap<Shading, Shader> shaders = new HashMap<Shading, Shader>();
	private HashMap<Shader, List<Entity>> shaderMap = new HashMap<Shader, List<Entity>>();
	
	private Vector3f clearColor = new Vector3f(0, 0, 0.4f);
	
	private int shadowBuffer = 0;
	private int shadowMap = 0;
	
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
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
		
		shadowBuffer = glGenFramebuffers();
		shadowMap = glGenTextures();
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

		renderScene(ct, camera);
	}
	
	private void renderScene(Transform transform, Camera camera) {
		glViewport(0, 0, Window.width, Window.height);

		projectionMatrix = camera.getProjectionMatrix();
		
		// Calculate view matrix
		viewMatrix.setIdentity();
		viewMatrix.rotate(Vector3f.negate(transform.rotation));
		viewMatrix.translate(Vector3f.negate(transform.position));
		
		shadowMap = renderShadowMap();
		
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
		
		shader = shaders.get(Shading.DIFFUSE);
		glUseProgram(shader.handle);

		for(Entity entity: shaderMap.get(shader)) {
			uploadLights(shader, pointLights, dirLights);
			drawMesh(shader, entity);
		}
		
		shader = shaders.get(Shading.NORMAL);
		glUseProgram(shader.handle);
		
		for(Entity entity: shaderMap.get(shader)) {
			uploadLights(shader, pointLights, dirLights);
			drawMesh(shader, entity);
		}
		
		shader = shaders.get(Shading.SPECULAR);
		glUseProgram(shader.handle);
		
		for(Entity entity: shaderMap.get(shader)) {
			Transform camT = (Transform) scene.mainCamera.getComponent(Transform.class);
			glUniform3f(shader.cameraPositionLoc, camT.position.x, camT.position.y, camT.position.z);
			
			uploadLights(shader, pointLights, dirLights);
			drawMesh(shader, entity);
		}
	}
	
	private int renderShadowMap() {
		// Render the scene for the shadow map
		Shader shader = shaders.get(Shading.SHADOW);
		glUseProgram(shader.handle);
		
		glBindTexture(GL_TEXTURE_2D, shadowMap);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, Window.width, Window.height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (FloatBuffer) null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer);
		glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowMap, 0);
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
		
		//FIXME multiple or no lights
		Transform transform = (Transform) scene.dirLights.get(0).owner.getComponent(Transform.class);
		Camera camera = new Camera(-10, 10, -10, 10, -20, 20);
		
		sprojectionMatrix = camera.getProjectionMatrix();

		// Calculate view matrix
		sviewMatrix.setIdentity();
		sviewMatrix.rotate(Vector3f.negate(transform.rotation));
		
		glDisable(GL_CULL_FACE);
		
		for(Entity entity: scene.getEntities()) {
			Mesh mesh = (Mesh) entity.getComponent(Mesh.class);
			
			if(mesh != null) {
				drawMesh(shader, entity);
			}
		}
		
		glEnable(GL_CULL_FACE);
		//glCullFace(GL_BACK);
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		return shadowMap;
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
	
	private void uploadLights(Shader shader, List<Entity> pointLights, List<Entity> dirLights) {
		uploadPointLights(shader, pointLights);
		uploadDirectionalLights(shader, dirLights);
	}
	
	/**
	 * Uploads all the point lights in the scene to the shaders
	 * @param shader The shader currently in use
	 * @param lights The list of entities that have a point light component
	 */
	private void uploadPointLights(Shader shader, List<Entity> pointLights) {
		glUniform1i(shader.numPointLightsLoc, pointLights.size());
		
		for(int i = 0; i < pointLights.size(); i++) {
			Entity e = pointLights.get(i);
			Transform  transform = (Transform) e.getComponent(Transform.class);
			PointLight light = (PointLight) e.getComponent(PointLight.class);

			glUniform3f(shader.plPositionLocs[i], transform.position.x, transform.position.y, transform.position.z);
			glUniform3f(shader.plColorLocs[i], light.color.x, light.color.y, light.color.z);
			glUniform3f(shader.plAttenuationLocs[i], light.attenuation.x, light.attenuation.y, light.attenuation.z);
		}
	}
	
	/**
	 * Uploads all the directional lights in the scene to the shaders
	 * @param shader The shader currently in use
	 * @param lights The list of entities that have a directional light component
	 */
	private void uploadDirectionalLights(Shader shader, List<Entity> dirLights) {
		glUniform1i(shader.numDirLightsLoc, dirLights.size());
		
		for(int i = 0; i < dirLights.size(); i++) {
			Entity e = dirLights.get(i);
			Transform  transform = (Transform) e.getComponent(Transform.class);
			DirectionalLight light = (DirectionalLight) e.getComponent(DirectionalLight.class);

			Matrix4f m = new Matrix4f();
			m.rotate(transform.rotation);
			Vector3f dir = new Vector3f(0, 0, -1);
			dir = m.transform(dir, 0);
			
			glUniform3f(shader.dlDirectionLocs[i], dir.x, dir.y, dir.z);
			glUniform3f(shader.dlColorLocs[i], light.color.x, light.color.y, light.color.z);
		}
	}
	
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
		if(mat.diffuseMap != null) {
			TextureData diffuseMap = AssetLoader.loadTexture(mat.diffuseMap);

			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, diffuseMap.handle);
			glUniform1i(shader.diffuseMapLoc, 0);

			// Let the shader know we uploaded a diffuse map
			glUniform1i(shader.hasDiffuseMapLoc, 1);
		} else {
			glUniform1i(shader.hasDiffuseMapLoc, 0);
		}
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
		
		glUniformMatrix4(shader.sprojectionMatrixLoc, false, sprojectionMatrix.getBuffer());
		glUniformMatrix4(shader.sviewMatrixLoc, false, sviewMatrix.getBuffer());

		if(mr.material != null) {
			uploadMaterial(shader, mr.material);
		}
		
		glActiveTexture(GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, shadowMap);
		glUniform1i(shader.shadowMapLoc, 3);
		
		glBindVertexArray(mesh.handle);
		glDrawArrays(GL_TRIANGLES, 0, mesh.getNumFaces() * 3);
		glBindVertexArray(0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, 0);
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, 0);
		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, 0);
		glActiveTexture(GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, 0);
	}
}
