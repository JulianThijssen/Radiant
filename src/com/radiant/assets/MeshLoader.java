package com.radiant.assets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.components.Mesh;
import com.radiant.exceptions.AssetLoaderException;
import com.radiant.geom.Face;

public class MeshLoader {
	public static final int VERTICES_PER_FACE = 3;
	
	public static Mesh loadMesh(String filepath) throws AssetLoaderException {
		String extension = filepath.substring(filepath.lastIndexOf('.'));
		if(".obj".equals(extension)) {
			return loadOBJ(filepath);
		}
		throw new AssetLoaderException("Can not open mesh file with extension: '" + extension + "'");
	}
	
	/** Support for v, no support for rational curves
	 * Support for vt, no support for w element
	 * Support for vn
	 * Support for f
	 * */
	private static Mesh loadOBJ(String filepath) throws AssetLoaderException {
		long time = System.currentTimeMillis();
		
		Mesh mesh = null;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(filepath)));
			
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
					mesh = new Mesh(name);
				}
				try {
					if(type.equals("v")) {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						if(mesh.vertices == null) {
							mesh.vertices = new ArrayList<Vector3f>();
						}
						mesh.vertices.add(new Vector3f(x, y, z));
					}
					if(type.equals("vt")) {
						float u = Float.parseFloat(segments[1]);
						float v = Float.parseFloat(segments[2]);
						if(mesh.textureCoords == null) {
							mesh.textureCoords = new ArrayList<Vector2f>();
						}
						mesh.textureCoords.add(new Vector2f(u, -v));
					}
					if(type.equals("vn")) {
						float x = Float.parseFloat(segments[1]);
						float y = Float.parseFloat(segments[2]);
						float z = Float.parseFloat(segments[3]);
						if(mesh.normals == null) {
							mesh.normals = new ArrayList<Vector3f>();
						}
						mesh.normals.add(new Vector3f(x, y, z));
					}
				} catch(NumberFormatException e) {
					in.close();
					throw new AssetLoaderException("Invalid coordinate at line: " + line);
				}
				
				if(type.equals("f")) {
					if(mesh == null) {
						mesh = new Mesh("Mesh");
					}
					if(mesh.faces == null) {
						mesh.faces = new ArrayList<Face>();
					}
					try {
						mesh.faces.add(readFace(segments));
					} catch(Exception e) {
						in.close();
						throw new AssetLoaderException(line + ": " + e.getMessage());
					}
				}
			}
			in.close();
			
			long dtime = System.currentTimeMillis();
			System.out.println(filepath+" : " + (dtime - time) + "ms");
			
			return mesh;
		} catch (Exception e) {
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
	
	private static String getPath(String filepath) {
		String path = "";
		int index = filepath.lastIndexOf('\\');
		if(index != -1) {
			path += filepath.substring(0, index+1);
		}
		return path;
	}
}
