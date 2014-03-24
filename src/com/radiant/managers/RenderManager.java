package com.radiant.managers;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.nio.FloatBuffer;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.Engine;
import com.radiant.Scene;
import com.radiant.assets.Image;
import com.radiant.assets.Material;
import com.radiant.assets.Object;
import com.radiant.assets.ShaderLoader;
import com.radiant.components.Component;
import com.radiant.components.Light;
import com.radiant.components.Mesh;
import com.radiant.components.Transform;
import com.radiant.entities.Entity;
import com.radiant.geom.Face;
import com.radiant.util.Log;

public class RenderManager implements Manager {
	private Engine engine;
	public int shader = ShaderLoader.loadShaders("res/shader.vert", "res/shader.frag");
	public HashMap<Object, Integer> vaoMap = new HashMap<Object, Integer>();
	
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
	
	public RenderManager(Engine engine) {
		this.engine = engine;
	}
	
	@Override
	public void create() {
		projectionLocation = GL20.glGetUniformLocation(shader, "projectionMatrix");
		viewLocation = GL20.glGetUniformLocation(shader, "viewMatrix");
		modelLocation = GL20.glGetUniformLocation(shader, "modelMatrix");
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		glClearColor(0.0f, 0.0f, 0.2f, 1.0f);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		
		genVaoMap();
	}
	
	@Override
	public void destroy() {
		
	}
	
	public void render() {
		Scene scene = engine.getScene();
		
		projectionMatrix = scene.mainCamera.getProjectionMatrix();
		projectionMatrix.store(projBuffer);
		projBuffer.flip();
		
		//Calculate view matrix
		viewMatrix.setIdentity();
				
		viewMatrix.store(viewBuffer);
		viewBuffer.flip();
		
		//Upload projection and view matrices to the shader
		GL20.glUniformMatrix4(projectionLocation, false, projBuffer);
		GL20.glUniformMatrix4(viewLocation, false, viewBuffer);
		
		//Render all entities
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		GL20.glUseProgram(shader);
		
		for(Entity entity: scene.entities) {
			renderEntity(entity);
		}
		
		GL20.glUseProgram(0);
	}
	
	public void renderEntity(Entity entity) {
		Transform transform = null;
		Mesh mesh = null;
		Light light = null;
		
		for(Component c: entity.components) {
			if(c instanceof Transform) {
				transform = (Transform) c;
			}
			if(c instanceof Mesh) {
				mesh = (Mesh) c;
			}
			if(c instanceof Light) {
				light = (Light) c;
			}
		}
		if(transform != null && light != null) {
			int lightLoc = GL20.glGetUniformLocation(shader, "lightPos");
			GL20.glUniform4f(lightLoc, transform.position.x, transform.position.y, transform.position.z, 1);
		}
		if(transform != null && mesh != null) {
			//Calculate model matrix
			modelMatrix.setIdentity();
			
			modelMatrix.translate(transform.position);
			modelMatrix.rotate(transform.rotation.x, axisX);
			modelMatrix.rotate(transform.rotation.y, axisY);
			transform.rotation.y++; //DELETE
			modelMatrix.rotate(transform.rotation.z, axisZ);
			modelMatrix.scale(transform.scale);
			
			modelBuffer.clear();
			modelMatrix.store(modelBuffer);
			modelBuffer.flip();
			
			GL20.glUniformMatrix4(modelLocation, false, modelBuffer);
			
			for(Object object: mesh.objects) {
				int vao = vaoMap.get(object);
				GL30.glBindVertexArray(vao);
				
				//If the object has a diffuse texture, upload it to the fragment shader
				Material material = mesh.materials.getMaterial(object.material);
				if(material != null) {
					if(material.diffuse != null) {
						Image image = material.diffuse.image;
						int diffuse = GL11.glGenTextures();
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, diffuse);
						GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
						GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.width, image.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image.data);
						GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
						
						//Bind the texture
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, diffuse);
					}
				}
				
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, object.faces.size() * 3);
				GL30.glBindVertexArray(0);
			}
		}
	}
	
	public void genVaoMap() {
		AssetManager am = engine.assetManager;
		for(Mesh mesh: am.meshes.values()) {
			for(int i = 0; i < mesh.objects.size(); i++) {
				Object object = mesh.objects.get(i);
				
				//Declare some variables
				FloatBuffer vertexBuffer = null;
				FloatBuffer textureBuffer = null;
				FloatBuffer normalBuffer = null;
				
				//FIXME make this variable
				int verticesPerFace = 3;
				
				//Create the vertex array object
				int vao = GL30.glGenVertexArrays();
				GL30.glBindVertexArray(vao);
				//Vertices
				if(mesh.vertices.size() > 0) {
					vertexBuffer = BufferUtils.createFloatBuffer(object.faces.size() * verticesPerFace * 3);
					
					//Store vertices in the vertex buffer
					for(Face face: object.faces) {
						for(int j = 0; j < verticesPerFace; j++) {
							Vector3f vertex = mesh.vertices.get(face.vi[j] - 1);
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
				
				//Textures
				if(mesh.textureCoords.size() > 0 && mesh.materials != null) {
					textureBuffer = BufferUtils.createFloatBuffer(object.faces.size() * verticesPerFace * 2);
					
					//Store the texture coordinates in the texcoord buffer
					for(Face face: object.faces) {
						for(int j = 0; j < verticesPerFace; j++) {
							Vector2f texture = mesh.textureCoords.get(face.ti[j] - 1);
							texture.store(textureBuffer);
						}
					}
					textureBuffer.flip();
					
					//Put the texture coordinate buffer into the VAO
					int textureVBO = GL15.glGenBuffers();
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureVBO);
					GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureBuffer, GL15.GL_STATIC_DRAW);
					GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
					GL20.glEnableVertexAttribArray(1);
				}
				//Normals
				if(mesh.normals.size() > 0) {
					normalBuffer = BufferUtils.createFloatBuffer(object.faces.size() * verticesPerFace * 3);
					
					//Store the normals in the normal buffer
					for(Face face: object.faces) {
						for(int j = 0; j < verticesPerFace; j++) {
							Vector3f normal = mesh.normals.get(face.ni[j] - 1);
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
				
				//Unbind the vao
				GL30.glBindVertexArray(0);
				
				//Add the vao to the vao map
				vaoMap.put(object, vao);
			}
		}
	}
}
