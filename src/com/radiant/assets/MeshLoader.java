package com.radiant.assets;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import com.radiant.util.Vector2f;
import com.radiant.util.Vector3f;

import com.radiant.exceptions.AssetLoaderException;
import com.radiant.geom.Face;

public class MeshLoader {
	public static final int VERTICES_PER_FACE = 3;
	
	protected static MeshData loadMesh(String filepath) throws AssetLoaderException {
		if(filepath.equals("Plane")) {
			return getPlane();
		}
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
				//FIXME NPE if file doesnt have 'o' or 'g'
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
						meshData.textureCoords.add(new Vector2f(u, -v));
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
			
			calculateNormals(meshData);
			calculateTangents(meshData);
			
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
	
	public static MeshData getPlane() {
		MeshData meshData = new MeshData("Plane");
		meshData.vertices = new ArrayList<Vector3f>();
		meshData.vertices.add(new Vector3f(-0.5f, -0.5f, 0));
		meshData.vertices.add(new Vector3f(0.5f, -0.5f, 0));
		meshData.vertices.add(new Vector3f(0.5f, 0.5f, 0));
		meshData.vertices.add(new Vector3f(-0.5f, 0.5f, 0));
		
		meshData.textureCoords = new ArrayList<Vector2f>();
		meshData.textureCoords.add(new Vector2f(0, 1));
		meshData.textureCoords.add(new Vector2f(1, 1));
		meshData.textureCoords.add(new Vector2f(1, 0));
		meshData.textureCoords.add(new Vector2f(0, 0));
		
		meshData.normals = new ArrayList<Vector3f>();
		meshData.normals.add(new Vector3f(0, 0, 1));
		
		meshData.faces = new ArrayList<Face>();
		Face face1 = new Face();
		face1.vi = new int[] {1, 2, 3};
		face1.ti = new int[] {1, 2, 3};
		face1.ni = new int[] {1, 1, 1};
		Face face2 = new Face();
		face2.vi = new int[] {1, 3, 4};
		face2.ti = new int[] {1, 3, 4};
		face2.ni = new int[] {1, 1, 1};
		meshData.faces.add(face1);
		meshData.faces.add(face2);
		
		calculateNormals(meshData);
		calculateTangents(meshData);
		
		meshData.handle = uploadMesh(meshData);
		return meshData;
	}
	
	private static void calculateNormals(MeshData mesh) {
		ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
		
		for(int i = 0; i < mesh.vertices.size(); i++) {
			normals.add(new Vector3f(0, 0, 0));
		}
		
		for(Face face: mesh.faces) {
			normals.get(face.vi[0] - 1).add(mesh.normals.get(face.ni[0] - 1));
			normals.get(face.vi[1] - 1).add(mesh.normals.get(face.ni[1] - 1));
			normals.get(face.vi[2] - 1).add(mesh.normals.get(face.ni[2] - 1));
		}
		
		for(int i = 0; i < normals.size(); i++) {
			normals.get(i).normalise();
		}
		mesh.normals = normals;
	}
	
	private static void calculateTangents(MeshData mesh) {
		if(mesh.tangents == null) {
			mesh.tangents = new ArrayList<Vector3f>();
		}

		for(int i = 0; i < mesh.vertices.size(); i++) {
			mesh.tangents.add(new Vector3f(0, 0, 0));
		}
		
		for(Face face: mesh.faces) {
			Vector3f v0 = mesh.vertices.get(face.vi[0] - 1);
			Vector3f v1 = mesh.vertices.get(face.vi[1] - 1);
			Vector3f v2 = mesh.vertices.get(face.vi[2] - 1);
			
			Vector2f u0 = mesh.textureCoords.get(face.ti[0] - 1);
			Vector2f u1 = mesh.textureCoords.get(face.ti[1] - 1);
			Vector2f u2 = mesh.textureCoords.get(face.ti[2] - 1);
			
			Vector3f Edge1 = Vector3f.sub(v1, v0);
			Vector3f Edge2 = Vector3f.sub(v2, v0);
			
			Vector2f dUV1 = Vector2f.sub(u1, u0);
			Vector2f dUV2 = Vector2f.sub(u2, u0);
			
			Vector3f tangent = new Vector3f();
			
			float det = 1.0f / (dUV1.x * dUV2.y - dUV1.y * dUV2.x);
			
			tangent.x = (dUV2.y * Edge1.x - dUV1.y * Edge2.x);
			tangent.y = (dUV2.y * Edge1.y - dUV1.y * Edge2.y);
			tangent.z = (dUV2.y * Edge1.z - dUV1.y * Edge2.z);

			tangent.scale(det);
			
			face.tai = new int[]{face.vi[0], face.vi[1], face.vi[2]};
			face.bti = new int[]{face.vi[0], face.vi[2], face.vi[2]};			
			
			for(int i = 0; i < VERTICES_PER_FACE; i++) {
				mesh.tangents.get(face.vi[0] - 1).add(tangent);
				mesh.tangents.get(face.vi[1] - 1).add(tangent);
				mesh.tangents.get(face.vi[2] - 1).add(tangent);
			}
		}
		
		for(int i = 0; i < mesh.tangents.size(); i++) {
			mesh.tangents.get(i).normalise();
		}
	}
	
	private static int uploadMesh(MeshData mesh) {
		//Declare some variables
		FloatBuffer vertexBuffer = null;
		FloatBuffer textureBuffer = null;
		FloatBuffer normalBuffer = null;
		FloatBuffer tangentBuffer = null;
		
		//Create the vertex array object
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
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
			int vertexVBO = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vertexVBO);
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glEnableVertexAttribArray(0);
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
			int textureVBO = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, textureVBO);
			glBufferData(GL_ARRAY_BUFFER, textureBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glEnableVertexAttribArray(1);
		}
		
		//Normals
		if(mesh.normals != null) {
			normalBuffer = BufferUtils.createFloatBuffer(mesh.faces.size() * VERTICES_PER_FACE * 3);
			
			//Store the normals in the normal buffer
			for(Face face: mesh.faces) {
				for(int j = 0; j < VERTICES_PER_FACE; j++) {
					Vector3f normal = mesh.normals.get(face.vi[j] - 1);
					normal.store(normalBuffer);
				}
			}
			normalBuffer.flip();
			
			//Put the normal buffer into the VAO
			int normalVBO = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, normalVBO);
			glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glEnableVertexAttribArray(2);
		}
		
		//Tangents
		if(mesh.tangents != null) {
			tangentBuffer = BufferUtils.createFloatBuffer(mesh.faces.size() * VERTICES_PER_FACE * 3);
			
			//Store the tangents in the tangent buffer
			for(Face face: mesh.faces) {
				for(int j = 0; j < VERTICES_PER_FACE; j++) {
					Vector3f tangent = mesh.tangents.get(face.tai[j] - 1);
					tangent.store(tangentBuffer);
				}
			}
			tangentBuffer.flip();
			
			//Put the tangent buffer into the VAO
			int tangentVBO = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, tangentVBO);
			glBufferData(GL_ARRAY_BUFFER, tangentBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glEnableVertexAttribArray(3);
		}
		
		//Unbind the vao
		glBindVertexArray(0);
		
		return vao;
	}
}
