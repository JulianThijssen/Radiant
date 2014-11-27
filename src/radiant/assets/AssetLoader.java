package radiant.assets;

import java.util.HashMap;
import java.util.List;

import radiant.assets.material.Material;
import radiant.assets.material.MaterialLoader;
import radiant.assets.model.Model;
import radiant.assets.model.ModelLoader;
import radiant.assets.shader.Shader;
import radiant.assets.shader.ShaderLoader;
import radiant.assets.texture.Texture;
import radiant.assets.texture.TextureData;
import radiant.assets.texture.TextureLoader;
import radiant.engine.core.diag.Log;
import radiant.engine.core.errors.AssetLoaderException;
import radiant.engine.core.file.Path;

public class AssetLoader {
	protected static HashMap<Path, Shader> shaderCache = new HashMap<Path, Shader>();
	protected static HashMap<Path, TextureData> textureCache = new HashMap<Path, TextureData>();
	protected static HashMap<Path, Model> meshCache = new HashMap<Path, Model>();
	
	private static Path basePath = new Path("res/");
	private static int errors = 0;
	
	public static int getErrors() {
		return errors;
	}
	
	public static Shader loadShader(Path path) {
		path = Path.concat(basePath, path);
		if(shaderCache.containsKey(path)) {
			return shaderCache.get(path);
		}
		try {
			Shader shader = ShaderLoader.loadShaders(path + ".vert", path + ".frag");
			shaderCache.put(path, shader);
			return shader;
		} catch(AssetLoaderException e) {
			Log.error(e.getMessage());
		}
		return null;
	}
	
	public static TextureData loadTexture(Texture texture) {
		if(textureCache.containsKey(texture.path)) {
			return textureCache.get(texture.path);
		}
		try {
			TextureData textureData = TextureLoader.loadTexture(texture);
			textureCache.put(texture.path, textureData);
			return textureData;
		} catch(AssetLoaderException e) {
			Log.error(e.getMessage());
		}
		return null;
	}
	
	public static List<Material> loadMaterials(Path path) {
		path = Path.concat(basePath, path);
		try {
			List<Material> materials = MaterialLoader.loadMTL(path);
			return materials;
		} catch(AssetLoaderException e) {
			Log.error(e.getMessage());
		}
		return null;
	}

	public static Model loadModel(Path path) {
		path = Path.concat(basePath, path);
		if(meshCache.containsKey(path)) {
			return meshCache.get(path);
		}
		try {
			Model model = ModelLoader.loadModel(path);
			meshCache.put(path, model);
			return model;
		} catch(AssetLoaderException e) {
			Log.error(e.getMessage());
		}
		return null;
	}
}
