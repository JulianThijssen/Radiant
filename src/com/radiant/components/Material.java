package com.radiant.components;

import org.lwjgl.util.vector.Vector3f;

import com.radiant.assets.Texture;

public class Material extends Component {
	private String name;
	private Vector3f diffuseColor = new Vector3f(1, 1, 1);
	private Vector3f specularColor = new Vector3f(1, 1, 1);
	private Vector3f ambientColor = new Vector3f(0, 0, 0);
	private int illumination = 0;

	public Texture diffuse = null;
	public Texture normal = null;
	public Texture specular = null;
	
	public Material(String name) {
		super("Material");
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDiffuseColor(float r, float g, float b) {
		diffuseColor.set(r, g, b);
	}
	
	public void setSpecularColor(float r, float g, float b) {
		specularColor.set(r, g, b);
	}
	
	public void setAmbientColor(float r, float g, float b) {
		ambientColor.set(r, g, b);
	}
	
	public void setIllumination(int illumination) {
		this.illumination = illumination;
	}
	
	public void setDiffuseTexture(Texture diffuse) {
		this.diffuse = diffuse;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name + "\n");
		sb.append(String.format("Kd %f %f %f\n", diffuseColor.x, diffuseColor.y, diffuseColor.z));
		sb.append(String.format("Ks %f %f %f\n", specularColor.x, specularColor.y, specularColor.z));
		sb.append(String.format("Ka %f %f %f\n", ambientColor.x, ambientColor.y, ambientColor.z));
		sb.append(String.format("illum %d\n", illumination));
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
