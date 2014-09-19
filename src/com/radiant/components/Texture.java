package com.radiant.components;

import com.radiant.util.Vector2f;

public class Texture extends Component {
	public String path;
	public int sampling;
	public Vector2f tiling = new Vector2f(1, 1);
	
	public Texture(String path, int sampling) {
		super("Texture");
		this.path = path;
		this.sampling = sampling;
	}
}
