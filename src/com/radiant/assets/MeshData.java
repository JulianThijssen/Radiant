package com.radiant.assets;

import java.util.ArrayList;

import com.radiant.util.Vector2f;
import com.radiant.util.Vector3f;

import com.radiant.geom.Face;

public class MeshData {
	public String name;
	public ArrayList<Vector3f> vertices = null;
	public ArrayList<Vector2f> textureCoords = null;
	public ArrayList<Vector3f> normals = null;
	public ArrayList<Vector3f> tangents = null;
	public ArrayList<Face> faces = null;
	public int handle;
	
	public MeshData(String name) {
		this.name = name;
	}
	
	public int getNumFaces() {
		return faces.size();
	}
	
	/*public void setPlane() {
		vertices = new ArrayList<Vector3f>();
		vertices.add(new Vector3f(-1, 0, -1));
		vertices.add(new Vector3f(-1, 0, 1));
		vertices.add(new Vector3f(1, 0, 1));
		vertices.add(new Vector3f(1, 0, -1));
		
		textureCoords = new ArrayList<Vector2f>();
		textureCoords.add(new Vector2f(0, 1));
		textureCoords.add(new Vector2f(0, 0));
		textureCoords.add(new Vector2f(1, 0));
		textureCoords.add(new Vector2f(1, 1));
		
		normals = new ArrayList<Vector3f>();
		normals.add(new Vector3f(0, 1, 0));
		normals.add(new Vector3f(0, 1, 0));
		normals.add(new Vector3f(0, 1, 0));
		normals.add(new Vector3f(0, 1, 0));
		
		faces = new ArrayList<Face>();
		Face face = new Face();
		face.vi = new int[]{0, 1, 2, 3};
		face.ti = new int[]{0, 1, 2, 3};
		face.ni = new int[]{0, 1, 2, 3};
		faces.add(face);
	}*/
}
