package com.radiant.util;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
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

import com.radiant.util.Log;

public class ShaderLoader {
	public static int loadShaders(String vertpath, String fragpath) {
		int vertexShader = loadShader(vertpath, GL_VERTEX_SHADER);
		int fragmentShader = loadShader(fragpath, GL_FRAGMENT_SHADER);
		
		int shaderProgram = glCreateProgram();
		
		System.out.println(GL20.glGetShaderInfoLog(vertexShader, 1000));
		System.out.println(GL20.glGetShaderInfoLog(fragmentShader, 1000));
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, fragmentShader);
		
		//Position information will be attribute 0
		glBindAttribLocation(shaderProgram, 0, "in_Position");
		//Normal information will be attribute 1
		glBindAttribLocation(shaderProgram, 1, "in_Normal");
		//Color information will be attribute 2
		glBindAttribLocation(shaderProgram, 2, "in_Color");
		
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
