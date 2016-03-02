package radiant.assets.shader;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.lwjgl.opengl.GL20;

import radiant.engine.core.diag.Log;
import radiant.engine.core.errors.AssetLoaderException;
import radiant.engine.core.file.FileIO;

public class ShaderLoader {
	public static final int LOG_SIZE = 1024;
	public static final String commonVert = FileIO.loadAsString("res/shaders/common.vert");
	public static final String commonFrag = FileIO.loadAsString("res/shaders/common.frag");
	
	public static Shader loadShaders(String vertpath, String fragpath) throws AssetLoaderException {
		Log.debug("Loading shader: " + vertpath + " and " + fragpath + " ...");
		int vertexShader = loadShader(vertpath, GL_VERTEX_SHADER);
		int fragmentShader = loadShader(fragpath, GL_FRAGMENT_SHADER);
		
		int shaderProgram = glCreateProgram();
		
		if(GL20.glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
			Log.error("Vertex shader: " + GL20.glGetShaderInfoLog(vertexShader, LOG_SIZE));
		}
		if(GL20.glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
			Log.error("Fragment shader: " + GL20.glGetShaderInfoLog(fragmentShader, LOG_SIZE));
		}
		
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		
		glLinkProgram(shaderProgram);
		glValidateProgram(shaderProgram);
		
		Shader shader = new Shader(shaderProgram);
		Log.debug("Successfully loaded shader: " + vertpath + " and " + fragpath);
		return shader;
	}
	
	public static int loadShader(String filename, int type) throws AssetLoaderException {
		StringBuilder shaderSource = new StringBuilder();
		if (type == GL_VERTEX_SHADER) {
			shaderSource.append(commonVert);
		}
		if (type == GL_FRAGMENT_SHADER) {
			shaderSource.append(commonFrag);
		}
		
		int shaderID = 0;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line = null;
			while((line = in.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			in.close();
		} catch(IOException e) {
			throw new AssetLoaderException("Could not load shader from: " + filename);
		}
		
		shaderID = glCreateShader(type);
		glShaderSource(shaderID, shaderSource);
		glCompileShader(shaderID);
		
		return shaderID;
	}
}
