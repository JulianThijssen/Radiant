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
}
