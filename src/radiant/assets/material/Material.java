package radiant.assets.material;

import radiant.assets.texture.Texture;
import radiant.engine.core.math.Vector2f;
import radiant.engine.core.math.Vector3f;

public class Material {
	public static final Shading DEFAULT_SHADING = Shading.DIFFUSE;
	public static final float DEFAULT_HARDNESS = 50;
	public static final float DEFAULT_TRANSPARENCY = 1;
	
	private String name;
	public Vector3f diffuseColor = new Vector3f(1, 1, 1);
	public Vector3f specularColor = new Vector3f(1, 1, 1);
	public Vector3f ambientColor = new Vector3f(0, 0, 0);
	
	public float specularIntensity = 1.0f;

	public Shading shading = DEFAULT_SHADING;
	public Vector2f tiling = new Vector2f(1, 1);
	
	public Texture diffuseMap = null;
	public Texture normalMap = null;
	public Texture specularMap = null;
	
	public float hardness = DEFAULT_HARDNESS;
	public float transparency = DEFAULT_TRANSPARENCY;
	
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
		if(diffuseMap != null) {
			sb.append(String.format("%s\n", diffuseMap.toString()));
		}
		if(normalMap != null) {
			sb.append(String.format("%s\n", normalMap.toString()));
		}
		if(specularMap != null) {
			sb.append(String.format("%s\n", specularMap.toString()));
		}
		return sb.toString();
	}
}
