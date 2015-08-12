package radiant.assets.material;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import radiant.assets.texture.Sampling;
import radiant.assets.texture.Texture;
import radiant.engine.core.errors.AssetLoaderException;
import radiant.engine.core.file.Path;
import radiant.engine.core.math.Vector2f;

public class MaterialLoader {
	public static List<Material> loadMTL(Path path) throws AssetLoaderException {
		List<Material> materials = new ArrayList<Material>();
		BufferedReader in = null;
		
		try {
			in = new BufferedReader(new FileReader(new File(path.toString())));
			
			Material currentMaterial = null;
			
			String line = null;
			while ((line = in.readLine()) != null) {
				String[] segments = getSegments(line);
				
				if (segments.length < 1) {
					continue;
				}
				
				String prefix = segments[0];
				
				if (prefix.equals("newmtl")) {
					String name = segments[1];
					currentMaterial = new Material(name);
					materials.add(currentMaterial);
				}
				
				// If no material has been made yet, continue parsing till one has
				if (currentMaterial == null) {
					continue;
				}
				try {
					// Diffuse color
					if (prefix.equals("Kd")) {
						currentMaterial.diffuseColor.x = Float.parseFloat(segments[1]);
						currentMaterial.diffuseColor.y = Float.parseFloat(segments[2]);
						currentMaterial.diffuseColor.z = Float.parseFloat(segments[3]);
					}
					// Specular color
					if (prefix.equals("Ks")) {
						currentMaterial.specularColor.x = Float.parseFloat(segments[1]);
						currentMaterial.specularColor.y = Float.parseFloat(segments[2]);
						currentMaterial.specularColor.z = Float.parseFloat(segments[3]);
					}
					// Ambient color
					if (prefix.equals("Ka")) {
						currentMaterial.ambientColor.x = Float.parseFloat(segments[1]);
						currentMaterial.ambientColor.y = Float.parseFloat(segments[2]);
						currentMaterial.ambientColor.z = Float.parseFloat(segments[3]);
					}
					// Specular intensity
					if (prefix.equals("Is")) {
						currentMaterial.specularIntensity = Float.parseFloat(segments[1]);
					}
					// Specular hardness
					if (prefix.equals("Ns")) {
						currentMaterial.hardness = Float.parseFloat(segments[1]);
					}
					// Transparency
					if(prefix.equals("d")) {
						currentMaterial.transparency = Float.parseFloat(segments[1]);
					}
				} catch(NumberFormatException e) {
					throw new AssetLoaderException("Invalid number at line: " + line);
				}
				
				// Diffuse texture
				if (prefix.equals("map_Kd")) {
					Path texpath = new Path(path.getCurrentFolder() + segments[1]);
					currentMaterial.diffuseMap = new Texture(texpath, Sampling.NEAREST);
				}
				
				// Normal texture
				if (prefix.equals("map_Kn")) {
					Path texpath = new Path(path.getCurrentFolder() + segments[1]);
					currentMaterial.normalMap = new Texture(texpath, Sampling.NEAREST);
				}
				
				// Specular texture
				if (prefix.equals("map_Ks")) {
					Path texpath = new Path(path.getCurrentFolder() + segments[1]);
					currentMaterial.specularMap = new Texture(texpath, Sampling.NEAREST);
				}
				
				// Tiling
				if(prefix.equals("tiling")) {
					currentMaterial.tiling = new Vector2f(Float.parseFloat(segments[1]), Float.parseFloat(segments[2]));
//					if(currentMaterial.diffuseMap != null) {
//						currentMaterial.diffuseMap.tiling = new Vector2f(Float.parseFloat(segments[1]), Float.parseFloat(segments[2]));
//					}
//					if(currentMaterial.diffuseMap != null) {
//						currentMaterial.normalMap.tiling = new Vector2f(Float.parseFloat(segments[1]), Float.parseFloat(segments[2]));
//					}
//					if(currentMaterial.diffuseMap != null) {
//						currentMaterial.specularMap.tiling = new Vector2f(Float.parseFloat(segments[1]), Float.parseFloat(segments[2]));
//					}
				}
				
				// Sampling
				if (prefix.equals("sampling")) {
					String sampling = segments[1];
					if(sampling.equals("nearest")) {
						currentMaterial.diffuseMap.sampling = Sampling.NEAREST;
					}
					if(sampling.equals("linear")) {
						currentMaterial.diffuseMap.sampling = Sampling.LINEAR;
					}
				}
				
				// Shadows
				if (prefix.equals("ReceiveShadows")) {
					String shadows = segments[1];
					if ("off".equals(shadows)) {
						currentMaterial.receiveShadows = false;
					}
					if ("on".equals(shadows)) {
						currentMaterial.receiveShadows = true;
					}
				}
				
				// Shading
				if (prefix.equals("shading")) {
					String shading = segments[1];
					if (shading.equals("unshaded")) {
						currentMaterial.shading = Shading.UNSHADED;
					}
					if (shading.equals("diffuse")) {
						currentMaterial.shading = Shading.DIFFUSE;
					}
					if (shading.equals("normal")) {
						currentMaterial.shading = Shading.NORMAL;
					}
					if (shading.equals("specular")) {
						currentMaterial.shading = Shading.SPECULAR;
					}
					if (shading.equals("debug")) {
						currentMaterial.shading = Shading.DEBUG;
					}
					if (shading.equals("reflective")) {
						currentMaterial.shading = Shading.REFLECTIVE;
					}
				}
			}
			materials.add(currentMaterial);
			
			return materials;
		} catch(FileNotFoundException fe) {
			throw new AssetLoaderException("Could not find file: " + path);
		} catch(IOException ie) {
			throw new AssetLoaderException("An error occurred while loading: " + path);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static String[] getSegments(String line) {
		line = line.trim();
		return line.split("\\s+");
	}
}
