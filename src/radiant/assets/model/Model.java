package radiant.assets.model;

import java.util.ArrayList;
import java.util.List;

import radiant.assets.material.Material;
import radiant.engine.components.Mesh;

public class Model {
	private List<Mesh> meshes = new ArrayList<Mesh>();
	public List<Material> materials = new ArrayList<Material>();
	
	public void addMesh(Mesh mesh) {
		meshes.add(mesh);
	}
	
	public void addMaterial(Material materialData) {
		materials.add(materialData);
	}
	
	public List<Mesh> getMeshes() {
		return meshes;
	}
	
	public List<Material> getMaterials() {
		return materials;
	}
}
