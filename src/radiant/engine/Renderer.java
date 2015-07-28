package radiant.engine;

import static org.lwjgl.opengl.GL11.glClearColor;

import java.util.HashMap;
import java.util.List;

import radiant.assets.material.Shading;
import radiant.assets.scene.Scene;
import radiant.assets.shader.Shader;
import radiant.engine.components.Camera;
import radiant.engine.components.Transform;
import radiant.engine.core.math.Matrix4f;
import radiant.engine.core.math.Vector3f;

public abstract class Renderer implements ISystem {
	protected Scene scene;
	
	protected Vector3f clearColor = new Vector3f(0, 0, 0);
	
	protected Matrix4f projectionMatrix = new Matrix4f();
	protected Matrix4f viewMatrix = new Matrix4f();
	protected Matrix4f modelMatrix = new Matrix4f();
	
	protected HashMap<Shading, Shader> shaders = new HashMap<Shading, Shader>();
	protected HashMap<Shader, List<Entity>> shaderMap = new HashMap<Shader, List<Entity>>();

	protected abstract void renderScene(Transform transform, Camera camera);
	
	/**
	 * Draws the mesh associated with the given entity
	 * @param shader The shader currently in use
	 * @param entity The entity that has the mesh component to be drawn
	 */
	protected abstract void drawMesh(Shader shader, Entity entity);
	
	
	/**
	 * Sets the scene that needs to be rendered
	 * @param scene The scene to be rendered
	 */
	public void setScene(Scene scene) {
		this.scene = scene;
	}
	
	/**
	 * Sets the clear color for the renderer
	 */
	public void setClearColor(float red, float green, float blue, float alpha) {
		glClearColor(red, green, blue, alpha);
	}
}
