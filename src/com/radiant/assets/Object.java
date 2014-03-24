package com.radiant.assets;

import java.util.ArrayList;

import com.radiant.geom.Face;

public class Object {
	private String name = null;
	
	public ArrayList<Face> faces = new ArrayList<Face>();
	public String material;
	
	public Object(String name) {
		this.name = name;
	}
}
