package com.radiant.assets;

import java.util.ArrayList;

import com.radiant.components.Component;

public class Model extends Component {
	public ArrayList<MeshData> meshes = new ArrayList<MeshData>();
	
	public Model() {
		super("Model");
	}
	
	public void addMesh(MeshData meshData) {
		meshData.model = this;
		meshes.add(meshData);
	}
}
