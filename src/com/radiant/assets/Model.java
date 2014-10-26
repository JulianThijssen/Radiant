package com.radiant.assets;

import java.util.ArrayList;

import com.radiant.util.Vector2f;
import com.radiant.util.Vector3f;

public class Model {
	public ArrayList<Mesh> meshes = new ArrayList<Mesh>();
	public ArrayList<Model> children = new ArrayList<Model>();
	private Model parent;
	
	public ArrayList<Vector3f> vertices = null;
	public ArrayList<Vector2f> textureCoords = null;
	public ArrayList<Vector3f> normals = null;
	public ArrayList<Vector3f> tangents = null;
	
	public void addChild(Model model) {
		model.parent = this;
		children.add(model);
	}
	
	public void addMesh(Mesh mesh) {
		mesh.parent = this;
		meshes.add(mesh);
	}
}
