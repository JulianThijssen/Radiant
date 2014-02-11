package com.radiant;

import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.glPolygonMode;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.components.Transform;
import com.radiant.entities.Entity;
import com.radiant.util.ShaderLoader;

public class SceneRenderer {
	public int shader = ShaderLoader.loadShaders("res/shader.vert", "res/shader.frag");
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f modelMatrix = new Matrix4f();
	
	private Scene scene;
	
	Vector3f axisX = new Vector3f(1, 0, 0);
	Vector3f axisY = new Vector3f(0, 1, 0);
	Vector3f axisZ = new Vector3f(0, 0, 1);
	
	public SceneRenderer(Scene scene) {
		this.scene = scene;
	}
	
	public void update() {
		int projLoc = GL20.glGetUniformLocation(shader, "projectionMatrix");
		int viewLoc = GL20.glGetUniformLocation(shader, "viewMatrix");
		int modelLoc = GL20.glGetUniformLocation(shader, "modelMatrix");
		
		GL20.glUseProgram(shader);
		
		FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
		projectionMatrix.store(projBuffer);
		projBuffer.flip();
		
		FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
		viewMatrix.store(viewBuffer);
		viewBuffer.flip();
		
		GL20.glUniformMatrix4(projLoc, false, projBuffer);
		GL20.glUniformMatrix4(viewLoc, false, viewBuffer);
		
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		
		for(Entity e: scene.entities) {
			Transform transform = e.transform;
			
			modelMatrix.translate(transform.position);
			modelMatrix.rotate(transform.rotation.x, axisX);
			modelMatrix.rotate(transform.rotation.y, axisY);
			modelMatrix.rotate(transform.rotation.z, axisZ);
			modelMatrix.scale(transform.scale);
			
			FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
			modelMatrix.store(modelBuffer);
			modelBuffer.flip();
			
			GL20.glUniformMatrix4(modelLoc, false, modelBuffer);
			
			GL30.glBindVertexArray(m.mesh);
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			
			//Draw the vertices
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, m.vertexCount);
			
			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL30.glBindVertexArray(0);
		}
		
		GL20.glUseProgram(0);
	}
}
