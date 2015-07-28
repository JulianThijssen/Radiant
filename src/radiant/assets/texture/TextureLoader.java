package radiant.assets.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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
	
	public static int create(int internalFormat, int width, int height, int format, int type, FloatBuffer data) {
		int texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, (FloatBuffer) data);
		
		return texture;
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
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glGenerateMipmap(GL_TEXTURE_2D);
		return handle;
	}
}
