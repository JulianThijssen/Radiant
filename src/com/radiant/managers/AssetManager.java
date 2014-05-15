package com.radiant.managers;

import java.util.HashMap;

import com.radiant.assets.TextureLoader;
import com.radiant.assets.MeshLoader;
import com.radiant.assets.Texture;
import com.radiant.components.Mesh;
import com.radiant.exceptions.AssetLoaderException;
import com.radiant.util.Log;

public class AssetManager {
	protected static HashMap<String, Texture> textureCache = new HashMap<String, Texture>();
	protected static HashMap<String, Mesh> meshCache = new HashMap<String, Mesh>();
	
	private static int errors = 0;
	
	public void create() {
		
	}
	
	public void destroy() {
		
	}
	
	public static int getErrors() {
		return errors;
	}
	
	public static Texture loadTexture(String path) {
		if(textureCache.containsKey(path)) {
			return textureCache.get(path);
		}
		try {
			Texture texture = TextureLoader.loadTexture(path);
			textureCache.put(path, texture);
			return texture;
		} catch (AssetLoaderException e) {
			Log.error(e.getMessage());
			errors++;
		}
		return null;
	}
	
	public static Mesh loadMesh(String path) {
		if(meshCache.containsKey(path)) {
			return meshCache.get(path);
		}
		try {
			Mesh mesh = MeshLoader.loadMesh(path);
			meshCache.put(path, mesh);
			return mesh;
		} catch (AssetLoaderException e) {
			Log.error(e.getMessage());
			errors++;
		}
		return null;
	}
}
