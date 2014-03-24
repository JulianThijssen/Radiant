package com.radiant.assets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.radiant.assets.Texture.TextureType;
import com.radiant.exceptions.AssetLoaderException;
import com.radiant.managers.AssetManager;

public class MaterialLoader {
	public static MaterialLibrary loadMTL(AssetManager am, String filepath) throws AssetLoaderException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(new File(filepath)));
			
			MaterialLibrary library = new MaterialLibrary();
			Material currentMaterial = null;
			
			String line = null;
			while((line = in.readLine()) != null) {
				String[] segments = getSegments(line);
				
				if(segments.length < 1) {
					continue;
				}
				
				String prefix = segments[0];
				
				if(prefix.equals("newmtl")) {
					currentMaterial = new Material(segments[1]);
					library.add(currentMaterial);
				}
				//If no material has been made yet, continue parsing till one has
				if(currentMaterial == null) {
					continue;
				}
				//Diffuse color
				if(prefix.equals("Kd")) {
					try {
						currentMaterial.diffuseColor.x = Float.parseFloat(segments[1]);
						currentMaterial.diffuseColor.y = Float.parseFloat(segments[2]);
						currentMaterial.diffuseColor.z = Float.parseFloat(segments[3]);
					} catch(NumberFormatException e) {
						throw new AssetLoaderException("Invalid diffuse color: " + line);
					}
				}
				//Specular color
				if(prefix.equals("Ks")) {
					try {
						currentMaterial.specularColor.x = Float.parseFloat(segments[1]);
						currentMaterial.specularColor.y = Float.parseFloat(segments[2]);
						currentMaterial.specularColor.z = Float.parseFloat(segments[3]);
					} catch(NumberFormatException e) {
						throw new AssetLoaderException("Invalid specular color: " + line);
					}
				}
				//Ambient color
				if(prefix.equals("Ka")) {
					try {
						currentMaterial.ambientColor.x = Float.parseFloat(segments[1]);
						currentMaterial.ambientColor.y = Float.parseFloat(segments[2]);
						currentMaterial.ambientColor.z = Float.parseFloat(segments[3]);
					} catch(NumberFormatException e) {
						throw new AssetLoaderException("Invalid ambient color: " + line);
					}
				}
				//Illumination model
				if(prefix.equals("illum")) {
					currentMaterial.illumination = Integer.parseInt(segments[1]);
				}
				//Diffuse texture
				if(prefix.equals("map_Kd")) {
					String imagepath = segments[1];
					currentMaterial.diffuse = new Texture(TextureType.DIFFUSE);
					currentMaterial.diffuse.image = am.getImage(getPath(filepath) + imagepath);
				}
			}
			return library;
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	private static String[] getSegments(String line) {
		line = line.trim();
		return line.split("\\s+");
	}
	
	private static String getPath(String filepath) {
		String path = "";
		int index = filepath.lastIndexOf('\\');
		if(index != -1) {
			path += filepath.substring(0, index+1);
		}
		return path;
	}
}
