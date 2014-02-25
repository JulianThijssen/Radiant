package com.radiant.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.components.Mesh;
import com.radiant.exceptions.OBJLoaderException;
import com.radiant.geom.Face;
import com.radiant.geom.Object;
import com.radiant.material.Material;

/** Support for v, no support for rational curves
 * Support for vt, no support for w element
 * Support for vn
 * Support for f
 * */

public class OBJLoader {
	public static Mesh loadMesh(String filepath) throws OBJLoaderException {
		//Make the mesh component to store all the data in
		Mesh mesh = new Mesh();
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(filepath)));
			
			int verticesPerFace = -1;
			
			Object currentObject = null;
			
			String line = null;
			while((line = in.readLine()) != null) {
				String[] segments = getSegments(line);
				
				//If the line consists of less than 1 segment it's not valid
				if(segments.length < 1) {
					continue;
				}
				
				String type = segments[0];
				
				//Load a material library
				if(type.equals("mtllib")) {
					String name = segments[1];
					
					mesh.materials = MTLLoader.load(getPath(filepath) + name);
				}
				if(type.equals("g")) {
					String name = segments[1];
					currentObject = new Object(name);
					mesh.objects.add(currentObject);
				}
				if(type.equals("v")) {
					try {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						
						mesh.vertices.add(new Vector3f(x, y, z));
						
					} catch(NumberFormatException e) {
						in.close();
						throw new OBJLoaderException("Invalid vertex coordinate at line: " + line);
					}
				}
				if(type.equals("vt")) {
					try {
						float u = Float.parseFloat(segments[1]);
						float v = Float.parseFloat(segments[2]);
						
						mesh.textureCoords.add(new Vector2f(u, v));
					} catch(NumberFormatException e) {
						in.close();
						throw new OBJLoaderException("Invalid texture coordinate at line: " + line);
					}
				}
				if(type.equals("vn")) {
					try {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						
						mesh.normals.add(new Vector3f(x, y, z));
					} catch(NumberFormatException e) {
						in.close();
						throw new OBJLoaderException("Invalid vertex normal at line: " + line);
					}
				}
				if(currentObject == null) {
					continue;
				}
				if(type.equals("usemtl")) {
					String name = segments[1];
					
					for(Material material: mesh.materials) {
						if(material.getName().equals(name)) {
							currentObject.material = material;
						}
					}
				}
				if(type.equals("f")) {
					try {
						verticesPerFace = segments.length - 1;
						Face face = new Face();
						face.vi = new int[verticesPerFace];
						face.ti = new int[verticesPerFace];
						face.ni = new int[verticesPerFace];
						for(int i = 0; i < verticesPerFace; i++) {
							String[] elements = segments[i+1].split("/");
							if(elements.length >= 1) {
								if(elements[0].isEmpty()) {
									throw new OBJLoaderException("Vertex missing for face at: " + line);
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
						currentObject.faces.add(face);
					} catch(NumberFormatException e) {
						in.close();
						throw new OBJLoaderException("Invalid face at line: " + line);
					}
				}
			}
			
			in.close();

			return mesh;
		} catch (IOException e) {
			throw new OBJLoaderException(e.getMessage());
		}
	}
	
	private static String[] getSegments(String line) {
		line = line.trim();
		return line.split("\\s+");
	}
	
	private static String getPath(String filepath) {
		String path = "";
		int index = filepath.lastIndexOf('/');
		if(index != -1) {
			path += filepath.substring(0, index+1);
		}
		return path;
	}
}
