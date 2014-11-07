package radiant.engine;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV_MODE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glTexEnvi;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;

import radiant.assets.AssetLoader;
import radiant.assets.material.Material;
import radiant.assets.material.Shading;
import radiant.assets.shader.Shader;
import radiant.assets.texture.TextureData;
import radiant.engine.components.AttachedTo;
import radiant.engine.components.Camera;
import radiant.engine.components.Light;
import radiant.engine.components.Mesh;
import radiant.engine.components.MeshRenderer;
import radiant.engine.components.Transform;
import radiant.engine.core.file.Path;
import radiant.engine.core.math.Matrix4f;
import radiant.engine.core.math.Vector3f;

public class Renderer {
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f modelMatrix = new Matrix4f();
	
	private FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
	
	private HashMap<Shading, Shader> shaders = new HashMap<Shading, Shader>();
	private HashMap<Shader, List<Entity>> shaderMap = new HashMap<Shader, List<Entity>>();
	
	private Vector3f clearColor = new Vector3f(0, 0, 0.4f);
	
	public Renderer() {
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
		
		// Load shaders
		shaders.put(Shading.NONE, null);
		shaders.put(Shading.UNSHADED, AssetLoader.loadShader(new Path("shaders/unshaded")));
		shaders.put(Shading.DIFFUSE, AssetLoader.loadShader(new Path("shaders/diffuse")));
		shaders.put(Shading.NORMAL, AssetLoader.loadShader(new Path("shaders/normal")));
		shaders.put(Shading.SPECULAR, AssetLoader.loadShader(new Path("shaders/specular")));
		
		for(Shader shader: shaders.values()) {
			shaderMap.put(shader, new ArrayList<Entity>());
		}
	}

	public void update(Scene scene, float interp) {
		// Render all entities
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
		ArrayList<Entity> lights = new ArrayList<Entity>();
		for(Entity e: scene.getEntities()) {
			Transform transform = (Transform) e.getComponent(Transform.class);
			Light light = (Light) e.getComponent(Light.class);
			if(transform != null && light != null) {
				lights.add(e);
			}
		}
		
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
		
		Shader shader = shaders.get(Shading.UNSHADED);
		glUseProgram(shader.handle);
		
		for(Entity entity: shaderMap.get(shader)) {
			drawMesh(shader, entity);
		}
		
		shader = shaders.get(Shading.DIFFUSE);
		glUseProgram(shader.handle);

		for(Entity entity: shaderMap.get(shader)) {
			uploadLights(shader, lights);
			drawMesh(shader, entity);
		}
		
		shader = shaders.get(Shading.NORMAL);
		glUseProgram(shader.handle);
		
		for(Entity entity: shaderMap.get(shader)) {
			uploadLights(shader, lights);
			drawMesh(shader, entity);
		}
		
		shader = shaders.get(Shading.SPECULAR);
		glUseProgram(shader.handle);
		
		for(Entity entity: shaderMap.get(shader)) {
			int camLoc = glGetUniformLocation(shader.handle, "camera_position");
			Transform camT = (Transform) scene.mainCamera.getComponent(Transform.class);
			glUniform3f(camLoc, camT.position.x, camT.position.y, camT.position.z);
			
			uploadLights(shader, lights);
			drawMesh(shader, entity);
		}
	}
	
	public void uploadLights(Shader shader, ArrayList<Entity> lights) {
		int numLights = glGetUniformLocation(shader.handle, "numLights");
		glUniform1i(numLights, lights.size());
		for(int i = 0; i < lights.size(); i++) {
			Entity e = lights.get(i);
			Transform  transform = (Transform) e.getComponent(Transform.class);
			Light light = (Light) e.getComponent(Light.class);

			int lightPos = glGetUniformLocation(shader.handle, "lights["+i+"].position");
			int lightColor = glGetUniformLocation(shader.handle, "lights["+i+"].color");
			int lightConstantAtt = glGetUniformLocation(shader.handle, "lights["+i+"].constantAtt");
			int lightLinearAtt = glGetUniformLocation(shader.handle, "lights["+i+"].linearAtt");
			int lightQuadraticAtt = glGetUniformLocation(shader.handle, "lights["+i+"].quadraticAtt");

			glUniform4f(lightPos, transform.position.x, transform.position.y, transform.position.z, 1);
			glUniform3f(lightColor, light.color.x, light.color.y, light.color.z);
			glUniform1f(lightConstantAtt, light.constantAtt);
			glUniform1f(lightLinearAtt, light.linearAtt);
			glUniform1f(lightQuadraticAtt, light.quadraticAtt);
		}
	}

	public void uploadMaterial(Shader shader, Material mat) {
		// Colors
		glUniform3f(glGetUniformLocation(shader.handle, "diffuseColor"),
													mat.diffuseColor.x,
													mat.diffuseColor.y,
													mat.diffuseColor.z);
		
		// Diffuse texture
		if(mat.diffuse != null) {
			TextureData diffuseMap = AssetLoader.loadTexture(mat.diffuse);

			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, diffuseMap.handle);
			glUniform1i(glGetUniformLocation(shader.handle, "diffuseMap"), 0);
			
			// Tiling
			glUniform2f(glGetUniformLocation(shader.handle, "tiling"),
											 mat.diffuse.tiling.x,
											 mat.diffuse.tiling.y);
			// Let the shader know we uploaded a diffuse map
			glUniform1i(glGetUniformLocation(shader.handle, "hasDiffuseMap"), 1);
		} else {
			glUniform1i(glGetUniformLocation(shader.handle, "hasDiffuseMap"), 0);
		}
		// Normal texture
		if(mat.normal != null) {
			TextureData normalMap = AssetLoader.loadTexture(mat.normal);

			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, normalMap.handle);
			glUniform1i(glGetUniformLocation(shader.handle, "normalMap"), 1);
			
			// Tiling
			glUniform2f(glGetUniformLocation(shader.handle, "tiling"),
											 mat.normal.tiling.x,
											 mat.normal.tiling.y);
			// Let the shader know we uploaded a normal map
			glUniform1i(glGetUniformLocation(shader.handle, "hasNormalMap"), 1);
		} else {
			glUniform1i(glGetUniformLocation(shader.handle, "hasNormalMap"), 0);
		}
		// Specular texture
		if(mat.specular != null) {
			TextureData specularMap = AssetLoader.loadTexture(mat.specular);

			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, specularMap.handle);
			glUniform1i(glGetUniformLocation(shader.handle, "specularMap"), 1);
			
			// Tiling
			glUniform2f(glGetUniformLocation(shader.handle, "tiling"),
											 mat.specular.tiling.x,
											 mat.specular.tiling.y);
			// Let the shader know we uploaded a normal map
			glUniform1i(glGetUniformLocation(shader.handle, "hasSpecularMap"), 1);
		} else {
			glUniform1i(glGetUniformLocation(shader.handle, "hasSpecularMap"), 0);
		}
	}
	
	public void drawMesh(Shader shader, Entity entity) {
		Transform transform = (Transform) entity.getComponent(Transform.class);
		Mesh mesh = (Mesh) entity.getComponent(Mesh.class);
		MeshRenderer mr = (MeshRenderer) entity.getComponent(MeshRenderer.class);
		AttachedTo attached = (AttachedTo) entity.getComponent(AttachedTo.class);
		
		if(transform == null) {
			return;
		}
		
		// Calculate model matrix
		modelMatrix.setIdentity();
		
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
