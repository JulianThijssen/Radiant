package radiant.engine.components;

import radiant.assets.material.Material;


public class MeshRenderer extends Component {
	public Material material;
	
	public MeshRenderer(Material material) {
		this.material = material;
	}
}
