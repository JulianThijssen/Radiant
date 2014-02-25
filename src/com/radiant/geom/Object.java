package com.radiant.geom;

import java.util.ArrayList;

import com.radiant.material.Material;

public class Object {
	private String name = null;
	
	public ArrayList<Face> faces = new ArrayList<Face>();
	public Material material = null;
	
	public Object(String name) {
		this.name = name;
	}
}
