package com.radiant.assets;

import java.util.ArrayList;
import java.util.HashMap;

public class AssetLoader {
	protected static ArrayList<String> loadQueue = new ArrayList<String>();
	protected static HashMap<String, TextureData> textureCache = new HashMap<String, TextureData>();
	protected static HashMap<String, MeshData> meshCache = new HashMap<String, MeshData>();
	
	private static int errors = 0;
	
	public static int getErrors() {
		return errors;
	}
	
	public static TextureData getTexture(String path) {
		if(textureCache.containsKey(path)) {
			return textureCache.get(path);
		}
		try {
			TextureData textureData = TextureLoader.loadTexture(path);
			textureCache.put(path, textureData);
			return textureData;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static MeshData getMesh(String path) {
		if(meshCache.containsKey(path)) {
			return meshCache.get(path);
		}
		try {
			MeshData meshData = MeshLoader.loadMesh(path);
			meshCache.put(path, meshData);
			return meshData;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
