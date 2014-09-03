package com.radiant.assets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.radiant.components.Material;
import com.radiant.exceptions.AssetLoaderException;

public class MaterialLoader {
	public static void loadMTL(String filepath) throws AssetLoaderException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(new File(filepath)));
			

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
					//library.add(currentMaterial);
				}
				//If no material has been made yet, continue parsing till one has
				if(currentMaterial == null) {
					continue;
				}
				try {
					//Diffuse color
					if(prefix.equals("Kd")) {
						currentMaterial.setDiffuseColor(Float.parseFloat(segments[1]), Float.parseFloat(segments[2]), Float.parseFloat(segments[3]));
					}
					//Specular color
					if(prefix.equals("Ks")) {
						currentMaterial.setSpecularColor(Float.parseFloat(segments[1]), Float.parseFloat(segments[2]), Float.parseFloat(segments[3]));
					}
					//Ambient color
					if(prefix.equals("Ka")) {
						currentMaterial.setAmbientColor(Float.parseFloat(segments[1]), Float.parseFloat(segments[2]), Float.parseFloat(segments[3]));
					}
				} catch(NumberFormatException e) {
					throw new AssetLoaderException("Invalid number at line: " + line);
				}
				
				//Diffuse texture
				if(prefix.equals("map_Kd")) {
					String imagepath = segments[1];
					//currentMaterial.diffuse = new Texture(imagepath);
				}
			}
			//return library;
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
		//return null;
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
