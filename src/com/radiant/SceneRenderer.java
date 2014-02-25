package com.radiant;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.components.Component;
import com.radiant.components.Light;
import com.radiant.components.Mesh;
import com.radiant.components.Transform;
import com.radiant.entities.Entity;
import com.radiant.geom.Face;
import com.radiant.geom.Object;
import com.radiant.material.Image;
import com.radiant.util.ShaderLoader;

public class SceneRenderer {
	public int shader = ShaderLoader.loadShaders("res/shader.vert", "res/shader.frag");
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f modelMatrix = new Matrix4f();
	
	private FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);
	private FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
	
	private int projectionLocation;
	private int viewLocation;
	private int modelLocation;
	
	private Scene scene;
	
	Vector3f axisX = new Vector3f(1, 0, 0);
	Vector3f axisY = new Vector3f(0, 1, 0);
	Vector3f axisZ = new Vector3f(0, 0, 1);
	
	public SceneRenderer(Scene scene) {
		this.scene = scene;
	}
	
	public void start() {
		projectionLocation = GL20.glGetUniformLocation(shader, "projectionMatrix");
		viewLocation = GL20.glGetUniformLocation(shader, "viewMatrix");
		modelLocation = GL20.glGetUniformLocation(shader, "modelMatrix");
		
		setProjectionMatrix(scene.mainCamera);
	}
	
	public void setProjectionMatrix(Camera camera) {
		projectionMatrix.m00 = (float) (1 / Math.tan(Math.toRadians(camera.fieldOfView / 2f)));
		projectionMatrix.m11 = (float) (1 / Math.tan(Math.toRadians(camera.fieldOfView / 2f)));
		projectionMatrix.m22 = -((camera.zFar + camera.zNear) / (camera.zFar - camera.zNear));
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * camera.zNear * camera.zFar) / (camera.zFar - camera.zNear));
		projectionMatrix.m33 = 0;
	}
	
	public void render() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		glClearColor(0.0f, 0.0f, 0.2f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		GL20.glUseProgram(shader);
		
		projectionMatrix.store(projBuffer);
		projBuffer.flip();
		
		viewMatrix.store(viewBuffer);
		viewBuffer.flip();
		
		GL20.glUniformMatrix4(projectionLocation, false, projBuffer);
		GL20.glUniformMatrix4(viewLocation, false, viewBuffer);
		
		//Calculate view matrix
		viewMatrix.setIdentity();
		
		//Lighting
		for (Entity entity: scene.entities) {
			Transform transform = null;
			Light light = null;
			
			for(Component c: entity.components) {
				if(c instanceof Transform) {
					transform = (Transform) c;
				}
				if(c instanceof Light) {
					light = (Light) c;
				}
			}
			
			if(transform == null || light == null) {
				continue;
			}
			
			int lightLoc = GL20.glGetUniformLocation(shader, "lightPos");
			GL20.glUniform4f(lightLoc, transform.position.x, transform.position.y, transform.position.z, 1);
		}
		
		//Mesh rendering
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

			//Draw the mesh
			drawMesh(mesh);
		}
		
		GL20.glUseProgram(0);
	}
	
	public void drawMesh(Mesh mesh) {
		//FIXME make this variable
		int verticesPerFace = 3;
		
		for(Object object: mesh.objects) {
			//Declare some variables
			int diffuse = -1;
			FloatBuffer vertexBuffer = null;
			FloatBuffer textureBuffer = null;
			FloatBuffer normalBuffer = null;
			
			//Generate the vertex array object
			int vao = GL30.glGenVertexArrays();
			System.out.println(vao);
			GL30.glBindVertexArray(vao);
			
			//Store the vertices in a float buffer
			if(mesh.vertices.size() > 0) {
				vertexBuffer = BufferUtils.createFloatBuffer(object.faces.size() * verticesPerFace * 3);
				
				for(Face face: object.faces) {
					for(int i = 0; i < verticesPerFace; i++) {
						Vector3f vertex = mesh.vertices.get(face.vi[i] - 1);
						vertex.store(vertexBuffer);
					}
				}
				vertexBuffer.flip();
				
				//Put the vertex buffer into the VAO
				int vertexVBO = GL15.glGenBuffers();
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBO);
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
				GL20.glEnableVertexAttribArray(0);
			}
			//Store the texture coordinates in a float buffer
			if(mesh.textureCoords.size() > 0) {
				textureBuffer = BufferUtils.createFloatBuffer(object.faces.size() * verticesPerFace * 2);
				
				for(Face face: object.faces) {
					for(int i = 0; i < verticesPerFace; i++) {
						Vector2f texture = mesh.textureCoords.get(face.ti[i] - 1);
						texture.store(textureBuffer);
					}
				}
				textureBuffer.flip();
				
				//Put the texture buffer into the VAO
				int textureVBO = GL15.glGenBuffers();
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureVBO);
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureBuffer, GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
				
				//If the object has a diffuse texture, upload it to the fragment shader
				if(object.material.diffuse != null) {
					Image image = object.material.diffuse.image;
					diffuse = GL11.glGenTextures();
					GL13.glActiveTexture(GL13.GL_TEXTURE0);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, diffuse);
					GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
					GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.width, image.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image.data);
					GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
					GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
				}
				GL20.glEnableVertexAttribArray(1);
			}
			//Store the normals in a float buffer
			if(mesh.normals.size() > 0) {
				normalBuffer = BufferUtils.createFloatBuffer(object.faces.size() * verticesPerFace * 3);
				
				for(Face face: object.faces) {
					for(int i = 0; i < verticesPerFace; i++) {
						Vector3f normal = mesh.normals.get(face.ni[i] - 1);
						normal.store(normalBuffer);
					}
				}
				normalBuffer.flip();
				
				//Put the normal buffer into the VAO
				int normalVBO = GL15.glGenBuffers();
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVBO);
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalBuffer, GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
				GL20.glEnableVertexAttribArray(2);
			}
			
			
			
			if(object.material.diffuse != null) {
				//Bind the texture
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, diffuse);
			}

			//Draw the vertices FIXME shouldn't have to divide by 3
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexBuffer.capacity()/3);
			
			GL20.glDisableVertexAttribArray(0);
			if(object.material.diffuse != null) {
				GL20.glDisableVertexAttribArray(1);
			}
			if(mesh.normals.size() > 0) {
				GL20.glDisableVertexAttribArray(2);
			}
			
			//Unbind the vao
			GL30.glBindVertexArray(0);
		}
	}
}
