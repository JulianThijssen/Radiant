package com.radiant.assets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.exceptions.AssetLoaderException;
import com.radiant.geom.Face;

public class MeshLoader {
	public static final int VERTICES_PER_FACE = 3;
	
	protected static MeshData loadMesh(String filepath) throws AssetLoaderException {
		String extension = filepath.substring(filepath.lastIndexOf('.'));
		try {
			if(".obj".equals(extension)) {
				return loadOBJ(filepath);
			}
		} catch(AssetLoaderException e) {
			throw new AssetLoaderException("Can not open mesh file with extension: '" + extension + "': " + e.getMessage());
		}
		return null;
	}
	
	/** Support for v, no support for rational curves
	 * Support for vt, no support for w element
	 * Support for vn
	 * Support for f
	 * */
	private static MeshData loadOBJ(String path) throws AssetLoaderException {
		long time = System.currentTimeMillis();
		
		MeshData meshData = null;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(path)));
			
			String line = null;
			while((line = in.readLine()) != null) {
				String[] segments = getSegments(line);
				
				//If the line consists of less than 1 segment it's not valid
				if(segments.length < 1) {
					continue;
				}
				
				String type = segments[0];
				
				if(type.equals("g") || type.equals("o")) {
					String name = (segments.length > 1) ? segments[1] : "Group";
					meshData = new MeshData(name);
				}
				try {
					if(type.equals("v")) {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						if(meshData.vertices == null) {
							meshData.vertices = new ArrayList<Vector3f>();
						}
						meshData.vertices.add(new Vector3f(x, y, z));
					}
					if(type.equals("vt")) {
						float u = Float.parseFloat(segments[1]);
						float v = Float.parseFloat(segments[2]);
						if(meshData.textureCoords == null) {
							meshData.textureCoords = new ArrayList<Vector2f>();
						}
						meshData.textureCoords.add(new Vector2f(u, v));
					}
					if(type.equals("vn")) {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						if(meshData.normals == null) {
							meshData.normals = new ArrayList<Vector3f>();
						}
						meshData.normals.add(new Vector3f(x, y, z));
					}
				} catch(NumberFormatException e) {
					in.close();
					throw new AssetLoaderException("Invalid coordinate at line: " + line);
				}
				
				if(type.equals("f")) {
					if(meshData == null) {
						meshData = new MeshData("Mesh");
					}
					if(meshData.faces == null) {
						meshData.faces = new ArrayList<Face>();
					}
					try {
						meshData.faces.add(readFace(segments));
					} catch(Exception e) {
						in.close();
						throw new AssetLoaderException(line + ": " + e.getMessage());
					}
				}
			}
			in.close();
			
			long dtime = System.currentTimeMillis();
			System.out.println(path + " : " + (dtime - time) + "ms");
			
			meshData.handle = uploadMesh(meshData);
			return meshData;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssetLoaderException(e.getMessage());
		}
	}
	
	private static Face readFace(String[] segments) throws AssetLoaderException {
		Face face = new Face();
		
		face.vi = new int[VERTICES_PER_FACE];
		face.ti = new int[VERTICES_PER_FACE];
		face.ni = new int[VERTICES_PER_FACE];
		for(int i = 0; i < VERTICES_PER_FACE; i++) {
			String[] elements = segments[i+1].split("/");
			if(elements.length >= 1) {
				if(elements[0].isEmpty()) {
					throw new AssetLoaderException("Vertex missing for face");
				}
				face.vi[i] = Integer.parseInt(elements[0]);
				
				if(elements.length >= 2) {
					if(!elements[1].isEmpty()) {
						face.ti[i] = Integer.parseInt(elements[1]);
					}
				}
				
				if(elements.length >= 3) {
					if(!elements[2].isEmpty()) {
						face.ni[i] = Integer.parseInt(elements[2]);
					}
				}
			}
		}
		return face;
	}
	
	private static String[] getSegments(String line) {
		line = line.trim();
		return line.split("\\s+");
	}
	
	private static int uploadMesh(MeshData mesh) {
		//Declare some variables
		FloatBuffer vertexBuffer = null;
		FloatBuffer textureBuffer = null;
		FloatBuffer normalBuffer = null;
		
		//Create the vertex array object
		int vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
		//Vertices
		if(mesh.vertices != null) {
			vertexBuffer = BufferUtils.createFloatBuffer(mesh.faces.size() * VERTICES_PER_FACE * 3);
			
			//Store vertices in the vertex buffer
			for(Face face: mesh.faces) {
				for(int j = 0; j < VERTICES_PER_FACE; j++) {
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
			textureBuffer = BufferUtils.createFloatBuffer(mesh.faces.size() * VERTICES_PER_FACE * 2);
			
			//Store the texture coordinates in the texcoord buffer
			for(Face face: mesh.faces) {
				for(int j = 0; j < VERTICES_PER_FACE; j++) {
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
			normalBuffer = BufferUtils.createFloatBuffer(mesh.faces.size() * VERTICES_PER_FACE * 3);
			
			//Store the normals in the normal buffer
			for(Face face: mesh.faces) {
				for(int j = 0; j < VERTICES_PER_FACE; j++) {
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
		
		return vao;
	}
}
