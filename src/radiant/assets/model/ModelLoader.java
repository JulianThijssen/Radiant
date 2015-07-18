package radiant.assets.model;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;

import radiant.assets.material.Material;
import radiant.assets.material.MaterialLoader;
import radiant.engine.components.Mesh;
import radiant.engine.core.diag.Clock;
import radiant.engine.core.diag.Log;
import radiant.engine.core.errors.AssetLoaderException;
import radiant.engine.core.file.Path;
import radiant.engine.core.math.Vector2f;
import radiant.engine.core.math.Vector3f;

public class ModelLoader {
	public static final int VERTICES_PER_FACE = 3;
	
	public static Model loadModel(Path path) throws AssetLoaderException {
		String extension = path.getExtension();

		if(".obj".equals(extension)) {
			return loadOBJ(path);
		}
		return null;
	}
	
	/** Support for v, no support for rational curves
	 * Support for vt, no support for w element
	 * Support for vn
	 * Support for f
	 * */
	private static Model loadOBJ(Path path) throws AssetLoaderException {
		BufferedReader in;
		
		Model model = new Model();
		Mesh currentMesh = null;
		
		model.materials = new ArrayList<Material>();
		
		ArrayList<Vector3f> vertices = null;
		ArrayList<Vector2f> texCoords = null;
		ArrayList<Vector3f> normals = null;
		
		Clock clock = new Clock();
		clock.start();
		
		try {
			in = new BufferedReader(new FileReader(new File(path.toString())));
		} catch(FileNotFoundException fnfe) {
			throw new AssetLoaderException("Could not find file: " + path.toString());
		}
		
		String line = null;
		
		//First phase scan, store v, vt, vn in the model
		try {
			while((line = in.readLine()) != null) {
				String[] segments = getSegments(line);
				
				//If the line consists of less than 1 segment it's not valid
				if(segments.length < 1) {
					continue;
				}
				
				String type = segments[0];
				
				try {
					if(type.equals("v")) {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						if(vertices == null) {
							vertices = new ArrayList<Vector3f>();
						}
						vertices.add(new Vector3f(x, y, z));
					}
					if(type.equals("vt")) {
						float u = Float.parseFloat(segments[1]);
						float v = Float.parseFloat(segments[2]);
						if(texCoords == null) {
							texCoords = new ArrayList<Vector2f>();
						}
						texCoords.add(new Vector2f(u, 1 - v));
					}
					if(type.equals("vn")) {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						if(normals == null) {
							normals = new ArrayList<Vector3f>();
						}
						normals.add(new Vector3f(x, y, z));
					}
				} catch(NumberFormatException e) {
					in.close();
					throw new AssetLoaderException("Invalid coordinate at line: " + line);
				}
			}
		
			// Second phase scan, gather mesh information, faces, materials
			try {
				in = new BufferedReader(new FileReader(new File(path.toString())));
			} catch(FileNotFoundException fnfe) {
				throw new AssetLoaderException("Could not find file: " + path.toString());
			}
		
			while((line = in.readLine()) != null) {
				String[] segments = getSegments(line);
				
				// If the line consists of less than 1 segment it's not valid
				if(segments.length < 1) {
					continue;
				}
				
				String type = segments[0];
				
				// A material library is present
				if(type.equals("mtllib")) {
					Path mtlPath = new Path(path.getCurrentFolder() + segments[1]);
					model.materials = MaterialLoader.loadMTL(mtlPath);
				}
				
				// A new group of faces starts
				if(type.equals("g") || type.equals("o")) {
					if(currentMesh != null) {
						loadVertices(vertices, currentMesh);
						loadTexCoords(texCoords, currentMesh);
						loadNormals(normals, currentMesh);
						//calculateNormals(currentMesh);
						loadTangents(currentMesh);
						model.addMesh(currentMesh);
					}
					String name = (segments.length > 1) ? segments[1] : "Group";
					currentMesh = new Mesh(name);
				}
				
				// This object is using the specified material from the material library
				if(type.equals("usemtl")) {
					if(currentMesh == null) {
						currentMesh = new Mesh("Mesh");
					}
					String mtlname = segments[1];
					for(int i = 0; i < model.materials.size(); i++) {
						Material material = model.materials.get(i);
						if(material.getName().equals(mtlname)) {
							currentMesh.materialIndex = i;
						}
					}
				}
				
					
				// A face is specified
				if(type.equals("f")) {
					if(currentMesh == null) {
						currentMesh = new Mesh("Mesh");
					}
					try {
						currentMesh.faces.add(readFace(segments));
					} catch(Exception e) {
						in.close();
						throw new AssetLoaderException(line + ": " + e.getMessage());
					}
				}
			}
			in.close();
		} catch(IOException ie) {
			throw new AssetLoaderException("An error occurred while reading: " + path);
		}
		
		// Do the processing on the last mesh added
		loadVertices(vertices, currentMesh);
		loadTexCoords(texCoords, currentMesh);
		loadNormals(normals, currentMesh);
		//calculateNormals(currentMesh);
		loadTangents(currentMesh);
		model.addMesh(currentMesh);
		
		// Third phase, upload the whole model
		uploadModel(model);
		
		clock.end();
		Log.info(path + " loaded in: " + clock.getMilliseconds() + "ms");
		
		return model;
	}
	
	private static void loadVertices(List<Vector3f> vertices, Mesh mesh) {
		if(vertices == null) {
			return;
		}
		if(mesh.vertices == null) {
			mesh.vertices = new ArrayList<Vector3f>();
		}
		for(Face face: mesh.faces) {
			Vector3f v1 = vertices.get(face.vi[0] - 1);
			Vector3f v2 = vertices.get(face.vi[1] - 1);
			Vector3f v3 = vertices.get(face.vi[2] - 1);
			
			mesh.vertices.add(v1);
			face.vi[0] = mesh.vertices.size();
			mesh.vertices.add(v2);
			face.vi[1] = mesh.vertices.size();
			mesh.vertices.add(v3);
			face.vi[2] = mesh.vertices.size();
		}
	}
	
	private static void loadTexCoords(List<Vector2f> texCoords, Mesh mesh) {
		if(texCoords == null) {
			return;
		}
		if(mesh.texCoords == null) {
			mesh.texCoords = new ArrayList<Vector2f>();
		}
		for(Face face: mesh.faces) {
			Vector2f t1 = texCoords.get(face.ti[0] - 1);
			Vector2f t2 = texCoords.get(face.ti[1] - 1);
			Vector2f t3 = texCoords.get(face.ti[2] - 1);
			
			mesh.texCoords.add(t1);
			face.ti[0] = mesh.texCoords.size();
			mesh.texCoords.add(t2);
			face.ti[1] = mesh.texCoords.size();
			mesh.texCoords.add(t3);
			face.ti[2] = mesh.texCoords.size();
		}
	}
	
	private static void loadNormals(List<Vector3f> normals, Mesh mesh) {
		if(normals == null) {
			return;
		}
		if(mesh.normals == null) {
			mesh.normals = new ArrayList<Vector3f>();
		}
		for(Face face: mesh.faces) {
			Vector3f n1 = normals.get(face.ni[0] - 1);
			Vector3f n2 = normals.get(face.ni[1] - 1);
			Vector3f n3 = normals.get(face.ni[2] - 1);
			
			mesh.normals.add(n1);
			face.ni[0] = mesh.normals.size();
			mesh.normals.add(n2);
			face.ni[1] = mesh.normals.size();
			mesh.normals.add(n3);
			face.ni[2] = mesh.normals.size();
		}
	}
	
	private static Face readFace(String[] segments) throws AssetLoaderException {
		Face face = new Face();
		
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
	
	private static void calculateNormals(Mesh mesh) {
		List<Vector3f> normals = new ArrayList<Vector3f>();
		
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
	
	private static void loadTangents(Mesh mesh) {
		if(mesh.vertices == null || mesh.texCoords == null) {
			return;
		}
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
			
			Vector2f u0 = mesh.texCoords.get(face.ti[0] - 1);
			Vector2f u1 = mesh.texCoords.get(face.ti[1] - 1);
			Vector2f u2 = mesh.texCoords.get(face.ti[2] - 1);
			
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
	
	private static void uploadModel(Model model) {
		//Declare some variables
		FloatBuffer vertexBuffer = null;
		FloatBuffer textureBuffer = null;
		FloatBuffer normalBuffer = null;
		FloatBuffer tangentBuffer = null;
		
		for(Mesh mesh: model.getMeshes()) {
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
			if(mesh.texCoords != null) {
				textureBuffer = BufferUtils.createFloatBuffer(mesh.faces.size() * VERTICES_PER_FACE * 2);
				
				//Store the texture coordinates in the texcoord buffer
				for(Face face: mesh.faces) {
					for(int j = 0; j < VERTICES_PER_FACE; j++) {
						Vector2f texture = mesh.texCoords.get(face.ti[j] - 1);
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
						Vector3f tangent = mesh.tangents.get(face.vi[j] - 1);
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
			
			mesh.handle = vao;
		}
	}
}
