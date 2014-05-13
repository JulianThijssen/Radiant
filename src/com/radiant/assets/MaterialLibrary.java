package com.radiant.assets;

import java.util.ArrayList;

import com.radiant.components.Material;

public class MaterialLibrary {
	public String name;
	public ArrayList<Material> materials = new ArrayList<Material>();
	
	public void add(Material material) {
		materials.add(material);
	}
	
	public Material getMaterial(String name) {
		for(Material material: materials) {
			if(name.equals(material.getName())) {
				return material;
			}
		}
		return null;
	}
}
