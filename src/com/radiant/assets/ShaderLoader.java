package com.radiant.assets;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.lwjgl.opengl.GL20;

import com.radiant.util.Log;

public class ShaderLoader {
	public static final int LOG_SIZE = 1024;
	
	public static Shader loadShaders(String vertpath, String fragpath) {
		int vertexShader = loadShader(vertpath, GL_VERTEX_SHADER);
		int fragmentShader = loadShader(fragpath, GL_FRAGMENT_SHADER);
		
		int shaderProgram = glCreateProgram();
		
		if(GL20.glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
			Log.error(GL20.glGetShaderInfoLog(vertexShader, LOG_SIZE));
		}
		if(GL20.glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
			Log.error(GL20.glGetShaderInfoLog(fragmentShader, LOG_SIZE));
		}
		
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		
		glLinkProgram(shaderProgram);
		glValidateProgram(shaderProgram);
		
		Shader shader = new Shader();
		shader.handle = shaderProgram;
		
		return shader;
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
