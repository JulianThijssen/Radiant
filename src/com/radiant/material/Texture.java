package com.radiant.material;

public class Texture {
	public TextureType type = null;
	public Image image = null;
	
	public Texture(TextureType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("Type: %s\n", type.toString()));
		sb.append(String.format("Image: %s\n", image.toString()));
		return sb.toString();
	}
	
	public enum TextureType {
		DIFFUSE, NORMAL, SPECULAR, AMBIENT;
	}
}
