package com.radiant;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.components.Component;
import com.radiant.components.Mesh;
import com.radiant.components.Transform;
import com.radiant.entities.Entity;
import com.radiant.util.ShaderLoader;

public class SceneRenderer {
	public int shader = ShaderLoader.loadShaders("res/shader.vert", "res/shader.frag");
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f modelMatrix = new Matrix4f();
	
	//Temporary light
	private Vector3f light = new Vector3f(0, 10, -15);
	
	private Scene scene;
	
	Vector3f axisX = new Vector3f(1, 0, 0);
	Vector3f axisY = new Vector3f(0, 1, 0);
	Vector3f axisZ = new Vector3f(0, 0, 1);
	
	public SceneRenderer(Scene scene) {
		this.scene = scene;
	}
	
	public void render() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		glClearColor(0.0f, 0.0f, 0.2f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		projectionMatrix = scene.mainCamera.getProjectionMatrix();
		
		int projLoc = GL20.glGetUniformLocation(shader, "projectionMatrix");
		int viewLoc = GL20.glGetUniformLocation(shader, "viewMatrix");
		int modelLoc = GL20.glGetUniformLocation(shader, "modelMatrix");
		//Temp
		int lightLoc = GL20.glGetUniformLocation(shader, "lightPos");
		
		GL20.glUseProgram(shader);
		
		FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
		projectionMatrix.store(projBuffer);
		projBuffer.flip();
		
		FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
		viewMatrix.store(viewBuffer);
		viewBuffer.flip();
		
		FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
		
		GL20.glUniformMatrix4(projLoc, false, projBuffer);
		GL20.glUniformMatrix4(viewLoc, false, viewBuffer);
		//Temp
		GL20.glUniform4f(lightLoc, light.x, light.y, light.z, 1);
		
		for(Entity entity: scene.entities) {
			Transform transform = null;
			Mesh mesh = null;
			
			for(Component c: entity.components) {
				if(c instanceof Transform) {
					transform = (Transform) c;
				}
				if(c instanceof Mesh) {
					mesh = (Mesh) c;
				}
			}
			
			if(transform == null || mesh == null) {
				continue;
			}
			
			modelMatrix.setIdentity();
			
			modelMatrix.translate(transform.position);
			modelMatrix.rotate(transform.rotation.x, axisX);
			modelMatrix.rotate(transform.rotation.y, axisY);
			modelMatrix.rotate(transform.rotation.z, axisZ);
			modelMatrix.scale(transform.scale);
			
			modelBuffer.clear();
			modelMatrix.store(modelBuffer);
			modelBuffer.flip();
			
			GL20.glUniformMatrix4(modelLoc, false, modelBuffer);
			
			GL30.glBindVertexArray(mesh.vao);
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			
			System.out.println(mesh.vertexCount);
			System.out.println(projectionMatrix.toString());
			System.out.println(viewMatrix.toString());
			System.out.println(modelMatrix.toString());
			//Draw the vertices FIXME shouldn't have to divide by 3
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, mesh.vertexCount/3);
			
			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL30.glBindVertexArray(0);
		}
		
		GL20.glUseProgram(0);
	}
}
