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
	private int shader;
	private int diffuseShader;
	private int normalShader;
	
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
		//Load all the shaders
		diffuseShader = ShaderLoader.loadShaders("res/shaders/diffuse.vert", "res/shaders/diffuse.frag");
		normalShader = ShaderLoader.loadShaders("res/shaders/normal.vert", "res/shaders/normal.frag");
		
		shader = diffuseShader;
		
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

	public void update(Scene scene, float interp) {
		//Render all entities
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
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
		
		//Lights
		ArrayList<Transform> lightspos = new ArrayList<Transform>();
		ArrayList<Light> lights = new ArrayList<Light>();
		
		for(Entity entity: scene.entities) {
			Transform transform = (Transform) entity.getComponent("Transform");
			Light light = (Light) entity.getComponent("Light");
			if(transform != null && light != null) {
				lightspos.add(transform);
				lights.add(light);
			}
		}
		
		//Meshes
		for(Entity entity: scene.entities) {
			Transform transform = (Transform) entity.getComponent("Transform");
			Mesh mesh = (Mesh) entity.getComponent("Mesh");
			Material material = (Material) entity.getComponent("Material");
			
			if(transform != null && mesh != null) {
				//If the object has a material, upload it to the fragment shader
				if(material != null) {
					if(material.diffuse != null) {
						shader = diffuseShader;
						GL20.glUseProgram(shader);
						TextureData texture = AssetLoader.getTexture(material.diffuse.path);
						GL13.glActiveTexture(GL13.GL_TEXTURE0);
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.handle);
						int loc = GL20.glGetUniformLocation(shader, "diffuse");
						GL20.glUniform1i(loc, 0);
					}
					if(material.diffuse != null && material.normal != null) {
						shader = normalShader;
						GL20.glUseProgram(shader);
						TextureData diffuseTexture = AssetLoader.getTexture(material.diffuse.path);
						TextureData normalTexture = AssetLoader.getTexture(material.normal.path);
						GL13.glActiveTexture(GL13.GL_TEXTURE0);
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, diffuseTexture.handle);
						int dloc = GL20.glGetUniformLocation(shader, "diffuse");
						GL20.glUniform1i(dloc, 0);
						GL13.glActiveTexture(GL13.GL_TEXTURE1);
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalTexture.handle);
						int nloc = GL20.glGetUniformLocation(shader, "normal");
						GL20.glUniform1i(nloc, 1);
					}
				}
				
				//Lights
				int numLights = GL20.glGetUniformLocation(shader, "numLights");
				GL20.glUniform1i(numLights, lightspos.size());
				for(int i = 0; i < lightspos.size(); i++) {	
					int lightPos = GL20.glGetUniformLocation(shader, "lights["+i+"].position");
					int lightColor = GL20.glGetUniformLocation(shader, "lights["+i+"].color");
					int lightConstantAtt = GL20.glGetUniformLocation(shader, "lights["+i+"].constantAtt");
					int lightLinearAtt = GL20.glGetUniformLocation(shader, "lights["+i+"].linearAtt");
					int lightQuadraticAtt = GL20.glGetUniformLocation(shader, "lights["+i+"].quadraticAtt");
					Transform  lightT = lightspos.get(i);
					Light light = lights.get(i);
					GL20.glUniform4f(lightPos, lightT.position.x, lightT.position.y, lightT.position.z, 1);
					GL20.glUniform3f(lightColor, light.color.x, light.color.y, light.color.z);
					GL20.glUniform1f(lightConstantAtt, light.constantAtt);
					GL20.glUniform1f(lightLinearAtt, light.linearAtt);
					GL20.glUniform1f(lightQuadraticAtt, light.quadraticAtt);
				}
				
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
				
				//Upload matrices to the shader
				projectionLocation = GL20.glGetUniformLocation(shader, "projectionMatrix");
				viewLocation = GL20.glGetUniformLocation(shader, "viewMatrix");
				modelLocation = GL20.glGetUniformLocation(shader, "modelMatrix");
				
				GL20.glUniformMatrix4(modelLocation, false, modelBuffer);
				GL20.glUniformMatrix4(projectionLocation, false, projBuffer);
				GL20.glUniformMatrix4(viewLocation, false, viewBuffer);
				
				MeshData data = AssetLoader.getMesh(mesh.path);
				GL30.glBindVertexArray(data.handle);
				
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, data.getNumFaces() * 3);
				GL30.glBindVertexArray(0);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
				GL20.glUseProgram(0);
			}
		}
	}
	
	public void renderEntity(Entity entity) {
		
	}
}
