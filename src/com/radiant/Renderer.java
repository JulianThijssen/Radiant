package com.radiant;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.assets.AssetLoader;
import com.radiant.assets.MeshData;
import com.radiant.assets.ShaderLoader;
import com.radiant.assets.TextureData;
import com.radiant.components.Camera;
import com.radiant.components.Light;
import com.radiant.components.Material;
import com.radiant.components.Mesh;
import com.radiant.components.Transform;
import com.radiant.entities.Entity;

public class Renderer {
	public int shader;
	
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f modelMatrix = new Matrix4f();
	
	private FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
	
	private int projectionLocation;
	private int viewLocation;
	private int modelLocation;
	
	Vector3f axisX = new Vector3f(1, 0, 0);
	Vector3f axisY = new Vector3f(0, 1, 0);
	Vector3f axisZ = new Vector3f(0, 0, 1);
	
	public Renderer() {
		shader = ShaderLoader.loadShaders("res/shader.vert", "res/shader.frag");
		projectionLocation = GL20.glGetUniformLocation(shader, "projectionMatrix");
		viewLocation = GL20.glGetUniformLocation(shader, "viewMatrix");
		modelLocation = GL20.glGetUniformLocation(shader, "modelMatrix");
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
		glClearColor(0.0f, 0.0f, 0.4f, 1.0f);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	public void update(Scene scene) {
		//Render all entities
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		GL20.glUseProgram(shader);
		
		Camera camera = (Camera) scene.mainCamera.getComponent("Camera");
		projectionMatrix = camera.getProjectionMatrix();
		projectionMatrix.store(projBuffer);
		projBuffer.flip();
		
		//Calculate view matrix
		viewMatrix.setIdentity();
		Transform ct = (Transform) scene.mainCamera.getComponent("Transform");
		viewMatrix.rotate(-ct.rotation.x, axisX);
		viewMatrix.rotate(-ct.rotation.y, axisY);
		viewMatrix.rotate(-ct.rotation.z, axisZ);
		viewMatrix.translate(ct.position.negate(null));
		viewMatrix.store(viewBuffer);
		viewBuffer.flip();
		
		//Upload projection and view matrices to the shader
		GL20.glUniformMatrix4(projectionLocation, false, projBuffer);
		GL20.glUniformMatrix4(viewLocation, false, viewBuffer);
		
		//Lights
		ArrayList<Transform> lightspos = new ArrayList<Transform>();
		ArrayList<Light> lightscolor = new ArrayList<Light>();
		
		for(Entity entity: scene.entities) {
			Transform transform = (Transform) entity.getComponent("Transform");
			Light light = (Light) entity.getComponent("Light");
			if(transform != null && light != null) {
				lightspos.add(transform);
				lightscolor.add(light);
			}
		}
		
		int numLights = GL20.glGetUniformLocation(shader, "numLights");
		GL20.glUniform1i(numLights, lightspos.size());
		for(int i = 0; i < lightspos.size(); i++) {	
			int lightPos = GL20.glGetUniformLocation(shader, "lights["+i+"].position");
			int lightColor = GL20.glGetUniformLocation(shader, "lights["+i+"].color");
			int lightConstantAtt = GL20.glGetUniformLocation(shader, "lights["+i+"].constantAtt");
			int lightLinearAtt = GL20.glGetUniformLocation(shader, "lights["+i+"].linearAtt");
			int lightQuadraticAtt = GL20.glGetUniformLocation(shader, "lights["+i+"].quadraticAtt");
			Transform transform = lightspos.get(i);
			Light light = lightscolor.get(i);
			GL20.glUniform4f(lightPos, transform.position.x, transform.position.y, transform.position.z, 1);
			GL20.glUniform3f(lightColor, light.color.x, light.color.y, light.color.z);
			GL20.glUniform1f(lightConstantAtt, light.constantAtt);
			GL20.glUniform1f(lightLinearAtt, light.linearAtt);
			GL20.glUniform1f(lightQuadraticAtt, light.quadraticAtt);
		}
		
		//Meshes
		for(Entity entity: scene.entities) {
			renderEntity(entity);
		}
		
		GL20.glUseProgram(0);
	}
	
	public void renderEntity(Entity entity) {
		Transform transform = (Transform) entity.getComponent("Transform");
		Mesh mesh = (Mesh) entity.getComponent("Mesh");
		Material material = (Material) entity.getComponent("Material");
		
		if(transform != null && mesh != null) {
			//Calculate model matrix
			modelMatrix.setIdentity();
			
			modelMatrix.translate(transform.position);
			modelMatrix.rotate(transform.rotation.x, axisX);
			modelMatrix.rotate(transform.rotation.y, axisY);
			modelMatrix.rotate(transform.rotation.z, axisZ);
			modelMatrix.scale(transform.scale);
			
			modelBuffer.clear();
			modelMatrix.store(modelBuffer);
			modelBuffer.flip();
			
			GL20.glUniformMatrix4(modelLocation, false, modelBuffer);
			
			MeshData data = AssetLoader.getMesh(mesh.path);
			GL30.glBindVertexArray(data.handle);
			
			//If the object has a material, upload it to the fragment shader
			if(material != null) {
				if(material.diffuse != null) {
					TextureData texture = AssetLoader.getTexture(material.diffuse.path);
					GL13.glActiveTexture(GL13.GL_TEXTURE0);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.handle);
					int loc = GL20.glGetUniformLocation(shader, "diffuse");
					GL20.glUniform1i(loc, 0);
				}
			}
			
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, data.getNumFaces() * 3);
			GL30.glBindVertexArray(0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
	}
}
