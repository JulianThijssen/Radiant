package com.radiant.assets;

import java.util.HashMap;

import com.radiant.components.Texture;

public class AssetLoader {
	protected static HashMap<String, Shader> shaderCache = new HashMap<String, Shader>();
	protected static HashMap<String, TextureData> textureCache = new HashMap<String, TextureData>();
	protected static HashMap<String, Model> meshCache = new HashMap<String, Model>();
	
	private static int errors = 0;
	
	public static int getErrors() {
		return errors;
	}
	
	public static Shader getShader(String path) {
		path = "res/shaders/" + path;
		if(shaderCache.containsKey(path)) {
			return shaderCache.get(path);
		}
		try {
			Shader shader = ShaderLoader.loadShaders(path + ".vert", path + ".frag");
			shaderCache.put(path, shader);
			return shader;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static TextureData getTexture(Texture texture) {
		if(textureCache.containsKey(texture.path)) {
			return textureCache.get(texture.path);
		}
		try {
			TextureData textureData = TextureLoader.loadTexture(texture);
			textureCache.put(texture.path, textureData);
			return textureData;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Model getMesh(String path) {
		if(meshCache.containsKey(path)) {
			return meshCache.get(path);
		}
		try {
			Model model = MeshLoader.loadMesh(path);
			meshCache.put(path, model);
			return model;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
