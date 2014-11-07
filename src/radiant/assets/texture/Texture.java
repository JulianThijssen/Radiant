package radiant.assets.texture;

import radiant.engine.core.file.Path;
import radiant.engine.core.math.Vector2f;

public class Texture {
	public Path path;
	public Sampling sampling;
	public Vector2f tiling = new Vector2f(1, 1);
	
	public Texture(Path path, Sampling sampling) {
		this.path = path;
		this.sampling = sampling;
	}
}
