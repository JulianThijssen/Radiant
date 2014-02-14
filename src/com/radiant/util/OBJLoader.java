package com.radiant.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.regex.Pattern;

import obj.radiant.exceptions.OBJImportException;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.components.Mesh;
import com.radiant.geom.Face;

/** Support for v, no support for rational curves
 * Support for vt, no support for w element
 * Support for vn
 * Support for f
 * */

public class OBJLoader {
	private Pattern vpattern = Pattern.compile("\\d+");
	private Pattern vtpattern = Pattern.compile("\\d+/\\d+");
	private Pattern vtnpattern = Pattern.compile("\\d+/\\d+/\\d+");
	
	private Pattern facePattern = null;
	
	public static Mesh loadMesh(String filepath) throws OBJImportException {
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(filepath)));
			
			ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
			ArrayList<Vector2f> textures = new ArrayList<Vector2f>();
			ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
			ArrayList<Face> faces = new ArrayList<Face>();
			int verticesPerFace = -1;
			
			String line = null;

			while((line = in.readLine()) != null) {
				String[] segments = getElements(line);
				
				//If the line consists of less than 1 segment it's not valid
				if(segments.length < 1) {
					continue;
				}
				
				String type = segments[0];
				
				if(type.equals("v")) {
					try {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						
						vertices.add(new Vector3f(x, y, z));
					} catch(NumberFormatException e) {
						in.close();
						throw new OBJImportException("Invalid vertex coordinate at line: " + line);
					}
				}
				if(type.equals("vt")) {
					try {
						float u = Float.parseFloat(segments[1]);
						float v = Float.parseFloat(segments[2]);
						
						textures.add(new Vector2f(u, v));
					} catch(NumberFormatException e) {
						in.close();
						throw new OBJImportException("Invalid texture coordinate at line: " + line);
					}
				}
				if(type.equals("vn")) {
					try {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						
						normals.add(new Vector3f(x, y, z));
					} catch(NumberFormatException e) {
						in.close();
						throw new OBJImportException("Invalid vertex normal at line: " + line);
					}
				}
				if(type.equals("f")) {
					if(segments[1])
					try {
						verticesPerFace = segments.length - 1;
						Face face = new Face();
						face.vertices = new int[verticesPerFace];
						face.textures = new int[verticesPerFace];
						face.normals = new int[verticesPerFace];
						for(int i = 0; i < verticesPerFace; i++) {
							String[] elements = segments[i+1].split("/");
							if(elements.length >= 1) {
								if(elements[0].isEmpty()) {
									throw new OBJImportException("Vertex missing for face at: " + line);
								}
								face.vertices[i] = Integer.parseInt(elements[0]);
								
								if(elements.length >= 2) {
									if(!elements[1].isEmpty()) {
										face.textures[i] = Integer.parseInt(elements[1]);
									}
								}
								
								if(elements.length >= 3) {
									if(!elements[2].isEmpty()) {
										face.normals[i] = Integer.parseInt(elements[2]);
									}
								}
							}
						}
						faces.add(face);
					} catch(NumberFormatException e) {
						in.close();
						throw new OBJImportException("Invalid face at line: " + line);
					}
				}
			}
			
			in.close();
			
			//Store the faces in a float buffer
			FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(faces.size() * verticesPerFace * 3);
			FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(faces.size() * verticesPerFace * 3);
			FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(faces.size() * verticesPerFace * 3);
			
			for(Face face: faces) {
				for(int i = 0; i < verticesPerFace; i++) {
					Vector3f vertex = vertices.get(face.vertices[i] - 1);
					vertex.store(vertexBuffer);
				}
				for(int i = 0; i < verticesPerFace; i++) {
					Vector2f texture = textures.get(face.textures[i] - 1);
					texture.store(textureBuffer);
				}
				for(int i = 0; i < verticesPerFace; i++) {
					Vector3f normal = normals.get(face.normals[i] - 1);
					normal.store(normalBuffer);
				}
			}
			vertexBuffer.flip();
			textureBuffer.flip();
			normalBuffer.flip();
			
			//Make the mesh component to store all the data in
			Mesh mesh = new Mesh();
			
			//Generate the vertex array object
			int vao = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vao);
			
			int vertexVBO = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexVBO);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			int textureVBO = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureVBO);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			int normalVBO = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalVBO);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			GL30.glBindVertexArray(0);
			
			mesh.vao = vao;
			mesh.vertexCount = vertexBuffer.capacity();
			return mesh;
		} catch (IOException e) {
			throw new OBJImportException(e.getMessage());
		}
	}
	
	private String[] getElements(String line) {
		//Remove leading and trailing whitespace
		line = line.trim();
		
		//Get all the elements delimited by a space character
		String[] elements = line.split(" ");
		
		//Put all elements that aren't empty in a list
		ArrayList<String> properElements = new ArrayList<String>();
		for(int i = 0; i < elements.length; i++) {
			if(!elements[i].isEmpty()) {
				properElements.add(elements[i]);
			}
		}
		
		//Return the proper elements as an array
		String[] properArray = new String[properElements.size()];
		for(int i = 0; i < properArray.length; i++) {
			properArray[i] = properElements.get(i);
		}
		return properArray;
	}
	
	private Pattern getPattern(String line) {
		String[] segments = getElements(line);
		if(vpattern.matcher(segments[1]).matches()) {return vpattern;}
		else if(vtpattern.matcher(segments[1]).matches()) {return vtpattern;}
		else if(vtnpattern.matcher(segments[1]).matches()) {return vtnpattern;}
		return null;
	}
}
