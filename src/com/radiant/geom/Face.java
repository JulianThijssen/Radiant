package com.radiant.geom;

public class Face {
	public int[] vertices;
	public int[] normals;
	
	public Face() {
		
	}
	
	public Face(int[] vertices, int[] normals) {
		this.vertices = vertices;
		this.normals = normals;
	}
}
