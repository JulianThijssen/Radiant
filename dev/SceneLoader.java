package radiant.assets.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import radiant.assets.AssetLoader;
import radiant.assets.model.Model;
import radiant.engine.Entity;
import radiant.engine.Scene;
import radiant.engine.components.AttachedTo;
import radiant.engine.components.Camera;
import radiant.engine.components.Light;
import radiant.engine.components.Mesh;
import radiant.engine.components.MeshRenderer;
import radiant.engine.components.Transform;
import radiant.engine.core.diag.Log;

public class SceneLoader {
	public static Scene loadScene(String path) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(new File(path)));
		} catch(FileNotFoundException e) {
			Log.error("Could not find scene file: " + path);
		}

		Queue<String> lines = new LinkedBlockingQueue<String>();
		
		String line = null;
		try {
			System.out.println("START READ");
			while((line = in.readLine()) != null) {
				lines.offer(line);
				System.out.println(line);
			}
			System.out.println("END READ");
			in.close();
		} catch(IOException e) {
			Log.error("An error occurred while reading: " + path);
		}
		
		Scene scene = new Scene();
		
		while((line = lines.poll()) != null) {
			if(line.startsWith("Entity")) {
				Entity entity = new Entity();
				System.out.println("Beep: " + line);
				String sad = lines.poll();
				System.out.println("Bep: " + sad);
				String[] tokens = sad.trim().split(":");

				if(tokens[0].equals("name")) {
					entity.name = tokens[1].trim();
					System.out.println("Entity with name: " + entity.name);
				}
				
				if(lines.poll().trim().startsWith("components")) {
					String comp;
					do {
						System.out.println("Lines: " + lines);
						System.out.println("")
					comp = lines.poll().trim();
					if(comp.startsWith("Transform")) {
						Transform transform = new Transform();
						for(int i = 0; i < 9; i++) {
						tokens = lines.poll().trim().split(":");
						if(tokens[0].equals("x")) {
							transform.position.x = Float.parseFloat(tokens[1].trim());
						}
						if(tokens[0].equals("y")) {
							transform.position.y = Float.parseFloat(tokens[1].trim());
						}
						if(tokens[0].equals("z")) {
							transform.position.z = Float.parseFloat(tokens[1].trim());
						}
						if(tokens[0].equals("rotX")) {
							transform.rotation.x = Float.parseFloat(tokens[1].trim());
						}
						if(tokens[0].equals("rotY")) {
							transform.rotation.x = Float.parseFloat(tokens[1].trim());
						}
						if(tokens[0].equals("rotZ")) {
							transform.rotation.x = Float.parseFloat(tokens[1].trim());
						}
						if(tokens[0].equals("scaleX")) {
							transform.scale.x = Float.parseFloat(tokens[1].trim());
						}
						if(tokens[0].equals("scaleY")) {
							transform.scale.y = Float.parseFloat(tokens[1].trim());
						}
						if(tokens[0].equals("scaleZ")) {
							transform.scale.z = Float.parseFloat(tokens[1].trim());
						}
						}
						entity.addComponent(transform);
						System.out.println("Transform added");
					}
					if(comp.startsWith("Model")) {
						tokens = lines.poll().trim().split(":");
						if(tokens[0].equals("path")) {
							String modelpath = tokens[1].trim();
							modelpath = modelpath.substring(1, modelpath.length() - 1);
							Model model = AssetLoader.getModel(modelpath);
							System.out.println(model);
							if(model.getMeshes().size() > 1) {
								for(Mesh mesh: model.getMeshes()) {
									Entity child = new Entity();
									child.addComponent(new AttachedTo(entity));
									child.addComponent(mesh);
									if(mesh.materialIndex == -1) {
										child.addComponent(new MeshRenderer(model.getMaterials().get(mesh.materialIndex)));
									}
								}
							} else {
								entity.addComponent(model.getMeshes().get(0));
								if(model.getMaterials().size() > 0) {
									entity.addComponent(new MeshRenderer(model.getMaterials().get(0)));
								}
							}
						}
						System.out.println("Model added");
					}
					if(comp.startsWith("Camera")) {
						Camera camera = new Camera();
						for(int i = 0; i < 4; i++) {
						tokens = lines.poll().trim().split(":");
						if(tokens[0].equals("fovy")) {
							camera.setFov(Float.parseFloat(tokens[1].trim()));
						}
						if(tokens[0].equals("aspect")) {
							camera.setAspectRatio(Float.parseFloat(tokens[1].trim()));
						}
						if(tokens[0].equals("zNear")) {
							camera.setZNear(Float.parseFloat(tokens[1].trim()));
						}
						if(tokens[0].equals("zFar")) {
							camera.setZFar(Float.parseFloat(tokens[1].trim()));
						}
						}
						entity.addComponent(camera);
						scene.mainCamera = entity;
						System.out.println("Camera added");
					}
					if(comp.startsWith("Light")) {
						Light light = new Light();
						for(int i = 0; i < 3; i++) {
							tokens = lines.poll().trim().split(":");
							if(tokens[0].equals("r")) {
								light.color.x = Float.parseFloat(tokens[1].trim());
							}
							if(tokens[0].equals("g")) {
								light.color.y = Float.parseFloat(tokens[1].trim());
							}
							if(tokens[0].equals("b")) {
								light.color.z = Float.parseFloat(tokens[1].trim());
							}
						}
						entity.addComponent(light);
						System.out.println("Light added");
					}
					} while(!comp.equals("}"));
				}
				scene.addEntity(entity);
			}
		}
		return scene;
	}
}
