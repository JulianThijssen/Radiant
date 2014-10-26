package com.radiant.assets;

import java.util.ArrayList;

import com.radiant.geom.Face;
import com.radiant.util.Vector2f;
import com.radiant.util.Vector3f;

public class Mesh {
	public String name;
	public ArrayList<Face> faces = null;
	
	public Model parent = null;
	public Material material = null;
	
	public int handle;
	
	public Mesh(String name) {
		this.name = name;
	}
	
	public int getNumFaces() {
		return faces.size();
	}
}
