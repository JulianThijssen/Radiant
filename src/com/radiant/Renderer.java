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
import com.radiant.assets.AssetLoader;
import com.radiant.assets.Material;
import com.radiant.assets.MeshData;
import com.radiant.assets.Model;
import com.radiant.assets.Shader;
import com.radiant.assets.TextureData;
import com.radiant.components.Camera;
import com.radiant.components.Light;
import com.radiant.components.Mesh;
import com.radiant.components.Transform;
import com.radiant.entities.Entity;

public class Renderer {
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f modelMatrix = new Matrix4f();
	
	private FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
	
	private HashMap<String, Shader> shaders = new HashMap<String, Shader>();
	private HashMap<Shader, List<MeshData>> shaderMap = new HashMap<Shader, List<MeshData>>();
	
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
		shaders.put("None", null);
		shaders.put("Unshaded", AssetLoader.getShader("unshaded"));
		shaders.put("Diffuse", AssetLoader.getShader("diffuse"));
		shaders.put("Normal", AssetLoader.getShader("normal"));
		
		for(Shader shader: shaders.values()) {
			shaderMap.put(shader, new ArrayList<MeshData>());
		}
	}

	public void update(Scene scene, float interp) {
		// Render all entities
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		Camera camera = (Camera) scene.mainCamera.getComponent("Camera");
		projectionMatrix = camera.getProjectionMatrix();
		projectionMatrix.store(projBuffer);
		projBuffer.flip();
		
		// Calculate view matrix
		viewMatrix.setIdentity();
		Transform ct = (Transform) scene.mainCamera.getComponent("Transform");
		viewMatrix.rotate(Vector3f.negate(ct.rotation));
		viewMatrix.translate(Vector3f.negate(ct.position));
		viewMatrix.store(viewBuffer);
		viewBuffer.flip();
		
		
		
		// Divide entities into light buckets
		ArrayList<Entity> lights = new ArrayList<Entity>();
		
		for(Entity e: scene.getEntities()) {
			Transform transform = (Transform) e.getComponent("Transform");
			Light light = (Light) e.getComponent("Light");
			if(transform != null && light != null) {
				lights.add(e);
			}
		}
		
		// Divide entities into shader buckets
		for(List<MeshData> meshes: shaderMap.values()) {
			meshes.clear();
		}
		for(Entity e: scene.getEntities()) {
			Mesh mesh = (Mesh) e.getComponent("Mesh");
			
			if(mesh != null) {
				Model model = AssetLoader.getMesh(mesh.path);
				for(MeshData data: model.meshes) {
					if(data.material != null) {
						Shader shader = shaders.get(data.material.shader);
						shaderMap.get(shader).add(data);
					}
				}
			}
		}
		
		Shader shader = shaders.get("Unshaded");
		glUseProgram(shader.handle);
		
		for(MeshData data: shaderMap.get(shader)) {
			uploadMesh(shader, data);
		}
		
		shader = shaders.get("Diffuse");
		glUseProgram(shader.handle);
		
		for(Entity e: shaderMap.get(shader)) {
			Transform transform = (Transform) e.getComponent("Transform");
			Mesh mesh = (Mesh) e.getComponent("Mesh");
				
			if(transform != null && mesh != null) {
				uploadLights(shader, lights);
				uploadMesh(shader, transform, mesh);
			}
		}
		
		shader = shaders.get("Normal");
		glUseProgram(shader.handle);
		
		for(Entity e: shaderMap.get(shader)) {
			Transform transform = (Transform) e.getComponent("Transform");
			Mesh mesh = (Mesh) e.getComponent("Mesh");
				
			if(transform != null && mesh != null) {
				uploadLights(shader, lights);
				uploadMesh(shader, transform, mesh);
			}
		}
	}
	
	public void uploadLights(Shader shader, ArrayList<Entity> lights) {
		int numLights = glGetUniformLocation(shader.handle, "numLights");
		glUniform1i(numLights, lights.size());
		for(int i = 0; i < lights.size(); i++) {
			Entity e = lights.get(i);
			Transform  transform = (Transform) e.getComponent("Transform");
			Light light = (Light) e.getComponent("Light");
			
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
	
	public void uploadMesh(Shader shader, MeshData mesh) {
		Transform transform = (Transform) mesh.model.parent.getComponent("Transform");
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
		
		Model model = AssetLoader.getMesh(mesh.path);
		
		for(MeshData data: model.meshes) {
			if(data.material != null) {
				uploadMaterial(shader, data.material);
			}
			
			
			glBindVertexArray(data.handle);
			glDrawArrays(GL_TRIANGLES, 0, data.getNumFaces() * 3);
			glBindTexture(GL_TEXTURE_2D, 0);
			
			glBindVertexArray(0);
		}
	}
}
