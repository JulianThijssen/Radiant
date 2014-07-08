package com.radiant.assets;

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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.radiant.util.Log;

public class ShaderLoader {
	public static final int LOG_SIZE = 1024;
	
	public static int loadShaders(String vertpath, String fragpath) {
		int vertexShader = loadShader(vertpath, GL_VERTEX_SHADER);
		int fragmentShader = loadShader(fragpath, GL_FRAGMENT_SHADER);
		
		int shaderProgram = glCreateProgram();
		
		if(GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			Log.error(GL20.glGetShaderInfoLog(vertexShader, LOG_SIZE));
		}
		if(GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			Log.error(GL20.glGetShaderInfoLog(fragmentShader, LOG_SIZE));
		}
		
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		
		glLinkProgram(shaderProgram);
		glValidateProgram(shaderProgram);
		
		return shaderProgram;
	}
	
	public static int loadShader(String filename, int type) {
		StringBuilder shaderSource = new StringBuilder();
		int shaderID = 0;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line = null;
			while((line = in.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			in.close();
		} catch(IOException e) {
			Log.debug("Could not load shader from: " + filename);
		}
		
		shaderID = glCreateShader(type);
		glShaderSource(shaderID, shaderSource);
		glCompileShader(shaderID);
		
		return shaderID;
	}
}
