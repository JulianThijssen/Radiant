package radiant.engine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;

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
	
	private FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
	
	private HashMap<Shading, Shader> shaders = new HashMap<Shading, Shader>();
	private HashMap<Shader, List<Entity>> shaderMap = new HashMap<Shader, List<Entity>>();
	
	private Vector3f clearColor = new Vector3f(0, 0, 0.4f);
	
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
	public void setGlParameters() {
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
	}
	
	/**
	 * Initialise all the shader buckets
	 */
	public void loadShaders() {
		shaders.put(Shading.NONE, null);
		shaders.put(Shading.UNSHADED, AssetLoader.loadShader(new Path("shaders/unshaded")));
		shaders.put(Shading.DIFFUSE, AssetLoader.loadShader(new Path("shaders/diffuse")));
		shaders.put(Shading.NORMAL, AssetLoader.loadShader(new Path("shaders/normal")));
		shaders.put(Shading.SPECULAR, AssetLoader.loadShader(new Path("shaders/specular")));
		
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
		projectionMatrix = camera.getProjectionMatrix();
		projectionMatrix.store(projBuffer);
		projBuffer.flip();
		
		// Calculate view matrix
		viewMatrix.setIdentity();
		Transform ct = (Transform) scene.mainCamera.getComponent(Transform.class);
		viewMatrix.rotate(Vector3f.negate(ct.rotation));
		viewMatrix.translate(Vector3f.negate(ct.position));
		viewMatrix.store(viewBuffer);
		viewBuffer.flip();
		
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
			int camLoc = glGetUniformLocation(shader.handle, "camera_position");
			Transform camT = (Transform) scene.mainCamera.getComponent(Transform.class);
			glUniform3f(camLoc, camT.position.x, camT.position.y, camT.position.z);
			
			uploadLights(shader, pointLights, dirLights);
			drawMesh(shader, entity);
		}
	}

	public void renderShadowMap() {
		int frameBuffer = 0;
		frameBuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
		
		int shadowMap;
		shadowMap = glGenTextures();
		
		glBindTexture(GL_TEXTURE_2D, shadowMap);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, Window.width, Window.height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
		
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowMap, 0);
		glDrawBuffer(GL_NONE);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			Log.debug("The framebuffer is not happy");
		}
	}
	
	/**
	 * Divide the meshes in the scene into their appropriate shader buckets
	 */
	public void divideMeshes() {
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
		int numLights = glGetUniformLocation(shader.handle, "numPointLights");
		glUniform1i(numLights, pointLights.size());
		for(int i = 0; i < pointLights.size(); i++) {
			Entity e = pointLights.get(i);
			Transform  transform = (Transform) e.getComponent(Transform.class);
			PointLight light = (PointLight) e.getComponent(PointLight.class);

			int lightPos = glGetUniformLocation(shader.handle, "pointLights["+i+"].position");
			int lightColor = glGetUniformLocation(shader.handle, "pointLights["+i+"].color");
			int lightAtt = glGetUniformLocation(shader.handle, "pointLights["+i+"].attenuation");

			glUniform3f(lightPos, transform.position.x, transform.position.y, transform.position.z);
			glUniform3f(lightColor, light.color.x, light.color.y, light.color.z);
			glUniform3f(lightAtt, light.attenuation.x, light.attenuation.y, light.attenuation.z);
		}
	}
	
	/**
	 * Uploads all the directional lights in the scene to the shaders
	 * @param shader The shader currently in use
	 * @param lights The list of entities that have a directional light component
	 */
	private void uploadDirectionalLights(Shader shader, List<Entity> dirLights) {
		int numLights = glGetUniformLocation(shader.handle, "numDirLights");
		glUniform1i(numLights, dirLights.size());
		for(int i = 0; i < dirLights.size(); i++) {
			Entity e = dirLights.get(i);
			Transform  transform = (Transform) e.getComponent(Transform.class);
			DirectionalLight light = (DirectionalLight) e.getComponent(DirectionalLight.class);

			int lightPos = glGetUniformLocation(shader.handle, "dirLights["+i+"].position");
			int lightColor = glGetUniformLocation(shader.handle, "dirLights["+i+"].color");
			int lightDir = glGetUniformLocation(shader.handle, "dirLights["+i+"].direction");

			Matrix4f m = new Matrix4f();
			m.rotate(transform.rotation);
			Vector3f dir = new Vector3f(0, -1, 0);
			dir = m.transform(dir, 0);
			
			glUniform3f(lightPos, transform.position.x, transform.position.y, transform.position.z);
			glUniform3f(lightColor, light.color.x, light.color.y, light.color.z);
			glUniform3f(lightDir, dir.x, dir.y, dir.z);
		}
	}
	
	/**
	 * Uploads the specified material to the shaders
	 * @param shader The shader currently in use
	 * @param mat    The material to be uploaded
	 */
	public void uploadMaterial(Shader shader, Material mat) {
		// Colors
		glUniform3f(glGetUniformLocation(shader.handle, "diffuseColor"),
			mat.diffuseColor.x, mat.diffuseColor.y, mat.diffuseColor.z);
		
		glUniform2f(glGetUniformLocation(shader.handle, "tiling"),
				    					mat.tiling.x, mat.tiling.y);
				
		// Diffuse texture
		if(mat.diffuseMap != null) {
			TextureData diffuseMap = AssetLoader.loadTexture(mat.diffuseMap);

			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, diffuseMap.handle);
			glUniform1i(glGetUniformLocation(shader.handle, "diffuseMap"), 0);
			
			// Tiling
			//glUniform2f(glGetUniformLocation(shader.handle, "tiling"),
			//			  mat.diffuseMap.tiling.x, mat.diffuseMap.tiling.y);
			
			// Let the shader know we uploaded a diffuse map
			glUniform1i(glGetUniformLocation(shader.handle, "hasDiffuseMap"), 1);
		} else {
			glUniform1i(glGetUniformLocation(shader.handle, "hasDiffuseMap"), 0);
		}
		// Normal texture
		if(mat.normalMap != null) {
			TextureData normalMap = AssetLoader.loadTexture(mat.normalMap);

			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, normalMap.handle);
			glUniform1i(glGetUniformLocation(shader.handle, "normalMap"), 1);
			
			// Tiling
			//glUniform2f(glGetUniformLocation(shader.handle, "tiling"),
			//			    mat.normalMap.tiling.x, mat.normalMap.tiling.y);
			
			// Let the shader know we uploaded a normal map
			glUniform1i(glGetUniformLocation(shader.handle, "hasNormalMap"), 1);
		} else {
			glUniform1i(glGetUniformLocation(shader.handle, "hasNormalMap"), 0);
		}
		// Specular texture
		if(mat.specularMap != null) {
			TextureData specularMap = AssetLoader.loadTexture(mat.specularMap);

			glActiveTexture(GL_TEXTURE2);
			glBindTexture(GL_TEXTURE_2D, specularMap.handle);
			glUniform1i(glGetUniformLocation(shader.handle, "specularMap"), 2);
			
			// Tiling
			//glUniform2f(glGetUniformLocation(shader.handle, "tiling"),
			//		mat.specularMap.tiling.x, mat.specularMap.tiling.y);
			// Let the shader know we uploaded a specular map
			glUniform1i(glGetUniformLocation(shader.handle, "hasSpecularMap"), 1);
		} else {
			glUniform1i(glGetUniformLocation(shader.handle, "hasSpecularMap"), 0);
		}
	}
	
	/**
	 * Draws the mesh associated with the given entity
	 * @param shader The shader currently in use
	 * @param entity The entity that has the mesh component to be drawn
	 */
	public void drawMesh(Shader shader, Entity entity) {
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
		
		modelBuffer.clear();
		modelMatrix.store(modelBuffer);
		modelBuffer.flip();
		
		// Upload matrices to the shader
		int projectionLocation = glGetUniformLocation(shader.handle, "projectionMatrix");
		int viewLocation = glGetUniformLocation(shader.handle, "viewMatrix");
		int modelLocation = glGetUniformLocation(shader.handle, "modelMatrix");
		
		glUniformMatrix4(modelLocation, false, modelBuffer);
		glUniformMatrix4(projectionLocation, false, projBuffer);
		glUniformMatrix4(viewLocation, false, viewBuffer);

		if(mr.material != null) {
			uploadMaterial(shader, mr.material);
		}
		
		glBindVertexArray(mesh.handle);
		glDrawArrays(GL_TRIANGLES, 0, mesh.getNumFaces() * 3);
		
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindVertexArray(0);
	}
}
