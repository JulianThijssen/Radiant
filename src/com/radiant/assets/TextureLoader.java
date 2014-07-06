package com.radiant.assets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.radiant.exceptions.AssetLoaderException;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class TextureLoader {
	protected static TextureData loadTexture(String filepath) throws AssetLoaderException {
		String extension = filepath.substring(filepath.lastIndexOf('.'));
		if(".png".equals(extension)) {
			return loadPNG(filepath);
		}
		throw new AssetLoaderException("Can not open texture file with extension: '" + extension + "'");
	}
	
	private static TextureData loadPNG(String filepath) throws AssetLoaderException {
		ByteBuffer buf = null;
		int width = 0;
		int height = 0;
		
		try {
			InputStream in = new FileInputStream(filepath);
			
			PNGDecoder decoder = new PNGDecoder(in);
			
			width = decoder.getWidth();
			height = decoder.getHeight();
			
			buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();
			
			in.close();
			
			TextureData texture = new TextureData();
			texture.width = width;
			texture.height = height;
			texture.buffer = buf;
			texture.handle = uploadTexture(texture);
			
			return texture;
		} catch (FileNotFoundException e) {
			throw new AssetLoaderException("Image was not found");
		} catch (IOException e) {
			throw new AssetLoaderException("An error occurred while loading the image");
		}
	}
	
	private static int uploadTexture(TextureData texture) {
		int handle = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, handle);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, texture.width, texture.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texture.buffer);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		return handle;
	}
}
