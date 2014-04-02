package com.radiant.assets;

public class Image {
	public int width, height;
	public int handle;
	
	public Image(int handle, int width, int height) {
		this.handle = handle;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String toString() {
		return width + "x" + height;
	}
}
