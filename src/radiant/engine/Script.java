package radiant.engine;

import radiant.assets.scene.Scene;

public interface Script {
	public void onStart(Scene scene);
	
	public void update(Scene scene);
	
	public void onStop(Scene scene);
}
