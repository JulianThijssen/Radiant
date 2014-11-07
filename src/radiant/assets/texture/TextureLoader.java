package radiant.assets.texture;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import radiant.engine.core.errors.AssetLoaderException;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class TextureLoader {
	public static TextureData loadTexture(Texture texture) throws AssetLoaderException {
		String extension = texture.path.getExtension();
		if(".png".equals(extension)) {
			return loadPNG(texture);
		}
		throw new AssetLoaderException("Can not open texture file with extension: '" + extension + "'");
	}
	
	private static TextureData loadPNG(Texture texture) throws AssetLoaderException {
		ByteBuffer buf = null;
		int width = 0;
		int height = 0;
		
		try {
			InputStream in = new FileInputStream(texture.path.toString());
			
			PNGDecoder decoder = new PNGDecoder(in);
			
			width = decoder.getWidth();
			height = decoder.getHeight();
			
			buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();
			
			in.close();
			
			TextureData textureData = new TextureData();
			textureData.width = width;
			textureData.height = height;
			textureData.buffer = buf;
			textureData.handle = uploadTexture(textureData, texture.sampling);
			
			return textureData;
		} catch (FileNotFoundException e) {
			throw new AssetLoaderException("Image was not found: " + texture.path);
		} catch (IOException e) {
			throw new AssetLoaderException("An error occurred while loading the image: " + texture.path);
		}
	}
	
	private static int uploadTexture(TextureData texture, Sampling sampling) {
		int handle = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, handle);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texture.width, texture.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, texture.buffer);
		if(sampling == Sampling.NEAREST) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		} else if(sampling == Sampling.LINEAR) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		}
		glGenerateMipmap(GL_TEXTURE_2D);
		return handle;
	}
}
