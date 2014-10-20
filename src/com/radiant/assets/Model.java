package com.radiant.assets;

import java.util.ArrayList;

import com.radiant.components.Component;

public class Model extends Component {
	public ArrayList<Mesh> meshes = new ArrayList<Mesh>();
	public ArrayList<Model> children = new ArrayList<Model>();
	private Model parent;
	
	public Model() {
		super("Model");
	}
	
	public void addChild(Model model) {
		model.parent = this;
		children.add(model);
	}
	
	public void addMesh(Mesh mesh) {
		meshes.add(mesh);
	}
}
