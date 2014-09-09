package com.radiant;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import com.radiant.util.Matrix4f;
import com.radiant.util.Vector3f;
import com.radiant.assets.AssetLoader;
import com.radiant.assets.MeshData;
import com.radiant.assets.Shader;
import com.radiant.assets.TextureData;
import com.radiant.components.Camera;
import com.radiant.components.Light;
import com.radiant.components.Material;
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
	
	private Shader shader;
	
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
	}

	public void update(Scene scene, float interp) {
		//Render all entities
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		shader = AssetLoader.getShader("res/shaders/default.frag");
		glUseProgram(shader.handle);
		
		Camera camera = (Camera) scene.mainCamera.getComponent("Camera");
		projectionMatrix = camera.getProjectionMatrix();
		projectionMatrix.store(projBuffer);
		projBuffer.flip();
		
		//Calculate view matrix
		viewMatrix.setIdentity();
		Transform ct = (Transform) scene.mainCamera.getComponent("Transform");
		viewMatrix.rotate(-ct.rotation.x, 1, 0, 0);
		viewMatrix.rotate(-ct.rotation.y, 0, 1, 0);
		viewMatrix.rotate(-ct.rotation.z, 0, 0, 1);
		viewMatrix.translate(Vector3f.negate(ct.position));
		viewMatrix.store(viewBuffer);
		viewBuffer.flip();
		
		//Lights
		ArrayList<Transform> lightspos = new ArrayList<Transform>();
		ArrayList<Light> lights = new ArrayList<Light>();
		
		for(Entity entity: scene.getEntities()) {
			Transform transform = (Transform) entity.getComponent("Transform");
			Light light = (Light) entity.getComponent("Light");
			if(transform != null && light != null) {
				lightspos.add(transform);
				lights.add(light);
			}
		}
		
		//Lights
		int numLights = glGetUniformLocation(shader.handle, "numLights");
		glUniform1i(numLights, lightspos.size());
		for(int i = 0; i < lightspos.size(); i++) {	
			int lightPos = glGetUniformLocation(shader.handle, "lights["+i+"].position");
			int lightColor = glGetUniformLocation(shader.handle, "lights["+i+"].color");
			int lightConstantAtt = glGetUniformLocation(shader.handle, "lights["+i+"].constantAtt");
			int lightLinearAtt = glGetUniformLocation(shader.handle, "lights["+i+"].linearAtt");
			int lightQuadraticAtt = glGetUniformLocation(shader.handle, "lights["+i+"].quadraticAtt");
			Transform  lightT = lightspos.get(i);
			Light light = lights.get(i);
			glUniform4f(lightPos, lightT.position.x, lightT.position.y, lightT.position.z, 1);
			glUniform3f(lightColor, light.color.x, light.color.y, light.color.z);
			glUniform1f(lightConstantAtt, light.constantAtt);
			glUniform1f(lightLinearAtt, light.linearAtt);
			glUniform1f(lightQuadraticAtt, light.quadraticAtt);
		}
		
		//Meshes
		for(Entity entity: scene.getEntities()) {
			Transform transform = (Transform) entity.getComponent("Transform");
			Mesh mesh = (Mesh) entity.getComponent("Mesh");
			Material material = (Material) entity.getComponent("Material");
			
			if(transform != null && mesh != null) {
				//If the object has a material, upload it to the fragment shader
				if(material != null) {
					//Colors
					glUniform3f(glGetUniformLocation(shader.handle, "diffuseColor"),
																	material.diffuseColor.x,
																	material.diffuseColor.y,
																	material.diffuseColor.z);
					
					//Textures
					if(material.diffuse != null) {
						TextureData diffuseMap = AssetLoader.getTexture(material.diffuse.path);
						glActiveTexture(GL_TEXTURE0);
						glBindTexture(GL_TEXTURE_2D, diffuseMap.handle);
						glUniform1i(glGetUniformLocation(shader.handle, "diffuseMap"), 0);
						//Tiling
						glUniform2f(glGetUniformLocation(shader.handle, "tiling"),
														 material.diffuse.tiling.x,
														 material.diffuse.tiling.y);
						//Let the shader know we uploaded a diffuse map
						glUniform1i(glGetUniformLocation(shader.handle, "hasDiffuseMap"), 1);
					} else {
						glUniform1i(glGetUniformLocation(shader.handle, "hasDiffuseMap"), 0);
					}
					
					if(material.normal != null) {
						TextureData normalMap = AssetLoader.getTexture(material.normal.path);
						glActiveTexture(GL_TEXTURE1);
						glBindTexture(GL_TEXTURE_2D, normalMap.handle);
						glUniform1i(glGetUniformLocation(shader.handle, "normalMap"), 1);
						//Tiling
						glUniform2f(glGetUniformLocation(shader.handle, "tiling"),
														 material.normal.tiling.x,
														 material.normal.tiling.y);
						//Let the shader know we uploaded a normal map
						glUniform1i(glGetUniformLocation(shader.handle, "hasNormalMap"), 1);
					} else {
						glUniform1i(glGetUniformLocation(shader.handle, "hasNormalMap"), 0);
					}
				}
				
				//Calculate model matrix
				modelMatrix.setIdentity();
				
				modelMatrix.translate(transform.position);
				modelMatrix.rotate(transform.rotation);
				modelMatrix.scale(transform.scale);
				
				modelBuffer.clear();
				modelMatrix.store(modelBuffer);
				modelBuffer.flip();
				
				//Upload matrices to the shader
				int projectionLocation = glGetUniformLocation(shader.handle, "projectionMatrix");
				int viewLocation = glGetUniformLocation(shader.handle, "viewMatrix");
				int modelLocation = glGetUniformLocation(shader.handle, "modelMatrix");
				
				glUniformMatrix4(modelLocation, false, modelBuffer);
				glUniformMatrix4(projectionLocation, false, projBuffer);
				glUniformMatrix4(viewLocation, false, viewBuffer);
				
				MeshData data = AssetLoader.getMesh(mesh.path);
				glBindVertexArray(data.handle);
				glDrawArrays(GL_TRIANGLES, 0, data.getNumFaces() * 3);
				
				glBindVertexArray(0);
				glBindTexture(GL_TEXTURE_2D, 0);
			}
		}
		glUseProgram(0);
	}
}
