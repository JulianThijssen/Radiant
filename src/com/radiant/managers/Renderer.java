package com.radiant.managers;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.Scene;
import com.radiant.Script;
import com.radiant.assets.ShaderLoader;
import com.radiant.components.Camera;
import com.radiant.components.Light;
import com.radiant.components.Material;
import com.radiant.components.Mesh;
import com.radiant.components.Transform;
import com.radiant.entities.Entity;
import com.radiant.geom.Face;

public class Renderer implements Script {
	public int shader = ShaderLoader.loadShaders("res/shader.vert", "res/shader.frag");
	public HashMap<Mesh, Integer> vaoMap = new HashMap<Mesh, Integer>();
	
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
	
	@Override
	public void onStart() {
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
		
		updateVaoMap();
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
		
		updateVaoMap();
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
			int lightpos = GL20.glGetUniformLocation(shader, "lights["+i+"].position");
			int lightcolor = GL20.glGetUniformLocation(shader, "lights["+i+"].color");
			int lightintensity = GL20.glGetUniformLocation(shader, "lights["+i+"].intensity");
			Transform transform = lightspos.get(i);
			Light light = lightscolor.get(i);
			GL20.glUniform4f(lightpos, transform.position.x, transform.position.y, transform.position.z, 1);
			GL20.glUniform3f(lightcolor, light.color.x, light.color.y, light.color.z);
			GL20.glUniform1f(lightintensity, light.intensity);
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
			
			Integer vao = vaoMap.get(mesh);
			if(vao == null) {
				return;
			}
			GL30.glBindVertexArray(vao);
			
			//If the object has a material, upload it to the fragment shader
			if(material != null) {
				if(material.diffuse != null) {
					GL13.glActiveTexture(GL13.GL_TEXTURE0);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.diffuse.handle);
					int loc = GL20.glGetUniformLocation(shader, "diffuse");
					GL20.glUniform1i(loc, 0);
				}
			}
			
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, mesh.faces.size() * 3);
			GL30.glBindVertexArray(0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
	}
	
	public void updateVaoMap() {
		for(Mesh mesh: AssetManager.meshCache.values()) {
			if(vaoMap.containsKey(mesh)) {
				return;
			}
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
			if(mesh.vertices != null) {
				vertexBuffer = BufferUtils.createFloatBuffer(mesh.faces.size() * verticesPerFace * 3);
				
				//Store vertices in the vertex buffer
				for(Face face: mesh.faces) {
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
			if(mesh.textureCoords != null) {
				textureBuffer = BufferUtils.createFloatBuffer(mesh.faces.size() * verticesPerFace * 2);
				
				//Store the texture coordinates in the texcoord buffer
				for(Face face: mesh.faces) {
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
			if(mesh.normals != null) {
				normalBuffer = BufferUtils.createFloatBuffer(mesh.faces.size() * verticesPerFace * 3);
				
				//Store the normals in the normal buffer
				for(Face face: mesh.faces) {
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
			vaoMap.put(mesh, vao);
		}
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		
	}
}
