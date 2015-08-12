package radiant.assets.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import radiant.engine.Entity;
import radiant.engine.Script;
import radiant.engine.components.Camera;
import radiant.engine.components.DirectionalLight;
import radiant.engine.components.Mesh;
import radiant.engine.components.MeshRenderer;
import radiant.engine.components.PointLight;
import radiant.engine.components.ReflectionProbe;
import radiant.engine.components.Transform;

public class Scene {
	private List<Entity> entities = new CopyOnWriteArrayList<Entity>();
	private ArrayList<Script> scripts = new ArrayList<Script>();
	
	public List<Transform> 		  transforms    = new ArrayList<Transform>();
	public List<Mesh>			  meshes 	    = new ArrayList<Mesh>();
	public List<MeshRenderer> 	  meshRenderers = new ArrayList<MeshRenderer>();
	public List<Camera>           cameras       = new ArrayList<Camera>();
	public List<PointLight>       pointLights   = new ArrayList<PointLight>();
	public List<DirectionalLight> dirLights     = new ArrayList<DirectionalLight>();
	public List<ReflectionProbe>  probes        = new ArrayList<ReflectionProbe>();
	
	public Entity mainCamera;
	
	/**
	 * Set the main camera from which to render the scene
	 * @param mainCamera The entity that contains the main camera component
	 */
	public void setMainCamera(Entity mainCamera) {
		this.mainCamera = mainCamera;
	}
	
	/** 
	 * Gets the points lights from the scene
	 */
	public List<Entity> getPointLights() {
		ArrayList<Entity> pointLights = new ArrayList<Entity>();
		for(Entity e: entities) {
			Transform transform = e.getComponent(Transform.class);
			PointLight light = e.getComponent(PointLight.class);
			if(transform != null && light != null) {
				pointLights.add(e);
			}
		}
		return pointLights;
	}
	
	/** 
	 * Gets the directional lights from the scene
	 */
	public List<Entity> getDirectionalLights() {
		ArrayList<Entity> dirLights = new ArrayList<Entity>();
		for(Entity e: entities) {
			Transform transform = e.getComponent(Transform.class);
			DirectionalLight light = e.getComponent(DirectionalLight.class);
			if(transform != null && light != null) {
				dirLights.add(e);
			}
		}
		return dirLights;
	}
	
	public void addEntity(Entity e) {
		entities.add(e);
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	public void addScript(Script s) {
		scripts.add(s);
	}
	
	public List<Script> getScripts() {
		return scripts;
	}
	
	public void start() {
		for(Script s: scripts) {
			s.onStart(this);
		}
	}
	
	public void update() {
		for(Script s: scripts) {
			s.update(this);
		}
	}
}
