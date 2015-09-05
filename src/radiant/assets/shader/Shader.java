package radiant.assets.shader;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;

import java.util.HashMap;

public class Shader {
	public static final int MAX_POINT_LIGHTS = 20;
	public static final int MAX_DIRECTIONAL_LIGHTS = 10;
	
	public int handle;
	
	public HashMap<String, Integer> locationMap = new HashMap<String, Integer>();

	public Shader(int handle) {
		this.handle = handle;
	}
	
	public int uniform(String location) {
		Integer loc = locationMap.get(location);
		if (loc == null) {
			loc = glGetUniformLocation(handle, location);
			locationMap.put(location, loc);
		}
		return loc;
	}
}
