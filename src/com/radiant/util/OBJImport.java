package com.radiant.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import obj.radiant.exceptions.OBJImportException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.geom.Face;
import com.radiant.geom.Model;

/** Support for v and vn 
 * No support for rational curves in 'v'
 * No support for vt
 * */

public class OBJImport {
	public static int load(String filepath) throws OBJImportException {
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(filepath)));
			
			Model model = new Model();
			ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
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
					try {
						verticesPerFace = segments.length - 1;
						Face face = new Face();
						face.vertices = new int[verticesPerFace];
						face.normals = new int[verticesPerFace];
						for(int i = 0; i < verticesPerFace; i++) {
							String[] elements = segments[i+1].split("/");
							face.vertices[i] = Integer.parseInt(elements[0]);
							if(elements.length == 2) {
								//Store vt
							}
							if(elements.length == 3) {
								if(!elements[1].isEmpty()) {
									//Store vt
								}
								face.normals[i] = Integer.parseInt(elements[2]);
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
			
			//Copy vertices to model
			model.vertices = new Vector3f[vertices.size()];
			for(int i = 0; i < model.vertices.length; i++) {
				model.vertices[i] = vertices.get(i);
			}
			
			//Copy normals to model
			model.normals = new Vector3f[normals.size()];
			for(int i = 0; i < model.normals.length; i++) {
				model.normals[i] = normals.get(i);
			}
			
			//Copy faces to model
			model.faces = new Face[faces.size()];
			for(int i = 0; i < model.faces.length; i++) {
				model.faces[i] = faces.get(i);
			}
			
			FloatBuffer faceBuffer = FloatBuffer.allocate(faces.size() * verticesPerFace);
			for(Face face: faces) {
				for(int i = 0; i < 3; i++) {
					Vector3f vertex = model.vertices[face.vertices[i] - 1];
					faceBuffer.put(vertex.x);
					faceBuffer.put(vertex.y);
					faceBuffer.put(vertex.z);
				}
			}
			faceBuffer.flip();
			
			int vao = GL30.glGenVertexArrays();
			GL30.glBindVertexArray(vao);
			
			int vertexBuffer = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, faceBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			int normalBuffer = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalBuffer);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, faceBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			
			GL30.glBindVertexArray(0);
			
			return vao;
		} catch (IOException e) {
			throw new OBJImportException(e.getMessage());
		}
	}
	
	private static String[] getElements(String line) {
		//Remove leading and trailing whitespace
		line = line.trim();
		
		//Get all the elements delimited by a space character
		String[] elements = line.split(" ");
		
		//Put all elements that arent empty in a list
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
}
