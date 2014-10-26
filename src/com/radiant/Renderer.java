package com.radiant;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;

import com.radiant.util.Matrix4f;
import com.radiant.util.Vector3f;
import com.radiant.assets.EntityMeshPair;
import com.radiant.assets.Material;
import com.radiant.assets.Mesh;
import com.radiant.assets.Model;
import com.radiant.assets.Shader;
import com.radiant.assets.Shading;
import com.radiant.assets.TextureData;
import com.radiant.assets.loader.AssetLoader;
import com.radiant.components.Camera;
import com.radiant.components.Light;
import com.radiant.components.ModelComponent;
import com.radiant.components.Transform;
import com.radiant.entities.Entity;

public class Renderer {
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f modelMatrix = new Matrix4f();
	
	private FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
	
	private HashMap<Shading, Shader> shaders = new HashMap<Shading, Shader>();
	private HashMap<Shader, List<EntityMeshPair>> shaderMap = new HashMap<Shader, List<EntityMeshPair>>();
	
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
		shaders.put(Shading.UNSHADED, AssetLoader.getShader("unshaded"));
		shaders.put(Shading.DIFFUSE, AssetLoader.getShader("diffuse"));
		shaders.put(Shading.NORMAL, AssetLoader.getShader("normal"));
		
		for(Shader shader: shaders.values()) {
			shaderMap.put(shader, new ArrayList<EntityMeshPair>());
		}
	}

	public void update(Scene scene, float interp) {
		// Render all entities
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
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
		for(List<EntityMeshPair> meshes: shaderMap.values()) {
			meshes.clear();
		}
		for(Entity e: scene.getEntities()) {
			ModelComponent modelcomp = (ModelComponent) e.getComponent(ModelComponent.class);
			if(modelcomp == null) {
				continue;
			}
			
			//FIXME wrong way to handle this
			Model model = AssetLoader.getModel(modelcomp.path);
			
			for(Mesh mesh: model.meshes) {
				if(mesh.material != null) {
					Shader shader = shaders.get(mesh.material.shading);
					shaderMap.get(shader).add(new EntityMeshPair(e, mesh));
				}
			}
		}
		
		Shader shader = shaders.get(Shading.UNSHADED);
		glUseProgram(shader.handle);
		
		for(EntityMeshPair emp: shaderMap.get(shader)) {
			uploadMesh(shader, emp.entity, emp.mesh);
		}
		
		shader = shaders.get(Shading.DIFFUSE);
		glUseProgram(shader.handle);
		
		for(EntityMeshPair emp: shaderMap.get(shader)) {
			uploadLights(shader, lights);
			uploadMesh(shader, emp.entity, emp.mesh);
		}
		
		shader = shaders.get(Shading.NORMAL);
		glUseProgram(shader.handle);
		
		for(EntityMeshPair emp: shaderMap.get(shader)) {
			uploadLights(shader, lights);
			uploadMesh(shader, emp.entity, emp.mesh);
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
			TextureData diffuseMap = AssetLoader.getTexture(mat.diffuse);

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
			TextureData normalMap = AssetLoader.getTexture(mat.normal);

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
	}
	
	public void uploadMesh(Shader shader, Entity entity, Mesh mesh) {
		Transform transform = (Transform) entity.getComponent(Transform.class);
		
		if(transform == null) {
			return;
		}
		
		// Calculate model matrix
		modelMatrix.setIdentity();
		
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
		

		if(mesh.material != null) {
			uploadMaterial(shader, mesh.material);
		}
		
		glBindVertexArray(mesh.handle);
		glDrawArrays(GL_TRIANGLES, 0, mesh.getNumFaces() * 3);
		
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindVertexArray(0);
	}
}
