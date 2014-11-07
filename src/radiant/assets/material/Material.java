package radiant.assets.material;

import radiant.assets.texture.Texture;
import radiant.engine.core.math.Vector3f;

public class Material {
	private String name;
	public Vector3f diffuseColor = new Vector3f(1, 1, 1);
	public Vector3f specularColor = new Vector3f(1, 1, 1);
	public Vector3f ambientColor = new Vector3f(0, 0, 0);

	public Shading shading = Shading.DIFFUSE;
	public Texture diffuse = null;
	public Texture normal = null;
	public Texture specular = null;
	
	public float hardness = 50;
	public float transparency = 1.0f;
	
	public Material(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setShading(Shading shading) {
		this.shading = shading;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name + "\n");
		sb.append(String.format("Kd %f %f %f\n", diffuseColor.x, diffuseColor.y, diffuseColor.z));
		sb.append(String.format("Ks %f %f %f\n", specularColor.x, specularColor.y, specularColor.z));
		sb.append(String.format("Ka %f %f %f\n", ambientColor.x, ambientColor.y, ambientColor.z));
		if(diffuse != null) {
			sb.append(String.format("%s\n", diffuse.toString()));
		}
		if(normal != null) {
			sb.append(String.format("%s\n", normal.toString()));
		}
		if(specular != null) {
			sb.append(String.format("%s\n", specular.toString()));
		}
		return sb.toString();
	}
}
