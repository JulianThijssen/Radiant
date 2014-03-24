package com.radiant.assets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.radiant.exceptions.AssetLoaderException;
import com.radiant.managers.AssetManager;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class ImageLoader {
	public static Image loadPNG(AssetManager am, String filepath) throws AssetLoaderException {
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
			
			return new Image(buf, width, height);
		} catch (FileNotFoundException e) {
			throw new AssetLoaderException("Image was not found");
		} catch (IOException e) {
			throw new AssetLoaderException("An error occurred while loading the image");
		}
	}
}
