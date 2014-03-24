package com.radiant.assets;

import java.nio.ByteBuffer;

public class Image {
	public int width, height;
	public ByteBuffer data;
	
	public Image(ByteBuffer data, int width, int height) {
		this.data = data;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String toString() {
		return width + "x" + height;
	}
}
