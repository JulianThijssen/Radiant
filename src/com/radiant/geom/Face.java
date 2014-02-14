package com.radiant.geom;

public class Face {
	public int[] vertices;
	public int[] textures;
	public int[] normals;
	
	public Face() {
		
	}
	
	public Face(int[] vertices) {
		this(vertices, null, null);
	}
	
	public Face(int[] vertices, int[] textures, int[] normals) {
		this.vertices = vertices;
		this.textures = textures;
		this.normals = normals;
	}
}
