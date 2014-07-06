package com.radiant.assets;

import java.nio.ByteBuffer;

public class TextureData {
	public int width, height;
	public ByteBuffer buffer;
	public int handle;
	
	public TextureData() {
		
	}
	
	@Override
	public String toString() {
		return "Tex: " + width + "x" + height;
	}
}
