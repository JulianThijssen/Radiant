package com.radiant.assets;

public class Texture {
	public int width, height;
	public int handle;
	
	public Texture() {
		
	}
	
	@Override
	public String toString() {
		return "Tex: " + width + "x" + height;
	}
}
