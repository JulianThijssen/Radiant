package com.radiant.components;

import com.radiant.SceneRenderer;
import com.radiant.geom.Mesh;

public class Renderer {
	public Mesh mesh;
	
	public void render(SceneRenderer sceneRenderer) {
		sceneRenderer.drawMesh(mesh);
	}
}
