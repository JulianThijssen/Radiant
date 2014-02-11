package com.radiant.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import obj.radiant.exceptions.OBJImportException;

import org.lwjgl.util.vector.Vector3f;

import com.radiant.geom.Face;
import com.radiant.geom.Model;

/** Support for v and vn 
 * No support for rational curves in 'v'
 * No support for vt
 * */

public class OBJImport {
	public static Model load(String filepath) throws OBJImportException {
		try {
			FileReader fr = new FileReader(new File(filepath));
			BufferedReader in = new BufferedReader(fr);
			
			Model model = new Model();
			ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
			ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
			ArrayList<Face> faces = new ArrayList<Face>();
			
			String line = null;

			while((line = in.readLine()) != null) {
				String[] segments = getElements(line);
				
				//If the line consists of less than 1 segment it's not valid
				if(segments.length < 1) {
					continue;
				}
				
				if(segments[0].equals("v")) {
					try {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						
						vertices.add(new Vector3f(x, y, z));
					} catch(NumberFormatException e) {
						e.printStackTrace();
						Log.error("Invalid vertex coordinate at line: " + line);
					}
				}
				if(segments[0].equals("vn")) {
					try {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						
						normals.add(new Vector3f(x, y, z));
					} catch(NumberFormatException e) {
						Log.error("Invalid vertex normal at line: " + line);
					}
				}
				if(segments[0].equals("f")) {
					try {
						int vertexCount = segments.length - 1;
						Face face = new Face();
						face.vertices = new int[vertexCount];
						face.normals = new int[vertexCount];
						for(int i = 0; i < vertexCount; i++) {
							String[] elements = segments[i+1].split("/");
							face.vertices[i] = Integer.parseInt(elements[0]);
							if(elements.length == 2) {
								//Store vt
							}
							if(elements.length == 3) {
								if(!elements[1].isEmpty()) {
									//Store vt
								}
								face.normals[i] = Integer.parseInt(elements[0]);
							}
						}
						faces.add(face);
					} catch(NumberFormatException e) {
						Log.error("Invalid face at line: " + line);
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
			
			return model;
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
