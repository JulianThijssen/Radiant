package com.radiant.assets;

import com.radiant.util.Vector2f;

public class Texture {
	public String path;
	public int sampling;
	public Vector2f tiling = new Vector2f(1, 1);
	
	public Texture(String path, int sampling) {
		this.path = path;
		this.sampling = sampling;
	}
}
