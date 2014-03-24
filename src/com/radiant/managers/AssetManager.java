package com.radiant.managers;

import java.io.File;
import java.util.HashMap;

import com.radiant.assets.Image;
import com.radiant.assets.ImageLoader;
import com.radiant.assets.MaterialLibrary;
import com.radiant.assets.MaterialLoader;
import com.radiant.assets.MeshLoader;
import com.radiant.components.Mesh;
import com.radiant.exceptions.AssetLoaderException;
import com.radiant.util.Log;

public class AssetManager implements Manager {
	public static final File DEFAULT_ROOT = new File("res");
	private File root;
	private HashMap<String, Image> images = new HashMap<String, Image>();
	private HashMap<String, MaterialLibrary> materials = new HashMap<String, MaterialLibrary>();
	public HashMap<String, Mesh> meshes = new HashMap<String, Mesh>();
	
	public AssetManager() {
		this(DEFAULT_ROOT);
	}
	
	public AssetManager(File root) {
		this.root = root;
	}
	
	public void create() {
		
	}
	
	public void destroy() {
		
	}
	
	public void loadAll() {
		loadImages(root);
		Log.info("Images loaded");
		loadMaterials(root);
		Log.info("Materials loaded");
		loadMeshes(root);
		Log.info("Meshes loaded");
	}
	
	public void loadImages(File file) {
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			for(int i = 0; i < files.length; i++) {
				loadImages(files[i]);
			}
			return;
		}
		
		String path = file.getPath();
		//FIXME
		String ext = path.substring(path.lastIndexOf("."));
		
		if(ext.equals(".png")) {
			try {
				this.images.put(path, ImageLoader.loadPNG(this, path));
			} catch(AssetLoaderException e) {
				Log.error("Failed to load image: " + path + ": " + e.getMessage());
			}
		}
	}
	
	public void loadMaterials(File file) {
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			for(int i = 0; i < files.length; i++) {
				loadMaterials(files[i]);
			}
			return;
		}
		
		String path = file.getPath();
		//FIXME
		String ext = path.substring(path.lastIndexOf("."));
		
		if(ext.equals(".mtl")) {
			try {
				this.materials.put(path, MaterialLoader.loadMTL(this, path));
			} catch(AssetLoaderException e) {
				Log.error("Failed to load material: " + path + ": " + e.getMessage());
			}
		}
	}
	
	public void loadMeshes(File file) {
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			for(int i = 0; i < files.length; i++) {
				loadMeshes(files[i]);
			}
			return;
		}
		
		String path = file.getPath();
		//FIXME
		String ext = path.substring(path.lastIndexOf("."));
		
		if(ext.equals(".obj")) {
			try {
				Mesh mesh = MeshLoader.loadOBJ(this, path);
				this.meshes.put(path, mesh);
			} catch(AssetLoaderException e) {
				Log.error("Failed to load mesh: " + path + ": " + e.getMessage());
			}
		}
	}
	
	public Image getImage(String path) throws AssetLoaderException {
		path = path.replace('/', '\\'); //FIXME
		if(!images.containsKey(path)) {
			throw new AssetLoaderException("Could not find image: " + path);
		}
		return images.get(path);
	}
	
	public MaterialLibrary getMaterials(String path) throws AssetLoaderException {
		path = path.replace('/', '\\'); //FIXME
		if(!materials.containsKey(path)) {
			throw new AssetLoaderException("Could not find material: " + path);
		}
		return materials.get(path);
	}
	
	public Mesh getMesh(String path) throws AssetLoaderException {
		path = path.replace('/', '\\'); //FIXME
		if(!meshes.containsKey(path)) {
			throw new AssetLoaderException("Could not find mesh: " + path);
		}
		return meshes.get(path);
	}
}
