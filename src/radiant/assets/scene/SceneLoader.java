package radiant.assets.scene;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import radiant.assets.AssetLoader;
import radiant.assets.material.Material;
import radiant.assets.material.MaterialLoader;
import radiant.assets.material.Shading;
import radiant.assets.model.Model;
import radiant.engine.Entity;
import radiant.engine.components.AttachedTo;
import radiant.engine.components.Camera;
import radiant.engine.components.DirectionalLight;
import radiant.engine.components.PointLight;
import radiant.engine.components.Mesh;
import radiant.engine.components.MeshRenderer;
import radiant.engine.components.MouseLook;
import radiant.engine.components.Transform;
import radiant.engine.core.diag.Log;
import radiant.engine.core.errors.AssetLoaderException;
import radiant.engine.core.file.Path;

public class SceneLoader {
	public static Scene loadScene(String path) {
		Document document = null;
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			document = builder.parse(new File(path));
		} catch(ParserConfigurationException pce) {
			Log.error("Failed to configure scene parser");
		} catch(IllegalArgumentException iae) {
			Log.error("Illegal argument to open scene file: " + path);
		} catch(SAXException saxe) {
			Log.error("Failed to parse scene file: " + path);
		} catch(IOException ie) {
			Log.error("An error occurred while reading scene file: " + path);
		}
		
		Scene scene = new Scene();
		
		Node sceneNode = document.getDocumentElement();
		
		for(int i = 0; i < sceneNode.getChildNodes().getLength(); i++) {
			Node entityNode = sceneNode.getChildNodes().item(i);
			if(entityNode.getNodeName().equals("Entity")) {
				Entity entity = new Entity();
				
				for(int j = 0; j < entityNode.getChildNodes().getLength(); j++) {
					Node propNode = entityNode.getChildNodes().item(j);
					
					if(propNode.getNodeName().equals("Name")) {
						entity.name = propNode.getTextContent();
					}
					
					if(propNode.getNodeName().equals("Components")) {
						for(int k = 0; k < propNode.getChildNodes().getLength(); k++) {
							Node compNode = propNode.getChildNodes().item(k);
							
							if(compNode.getNodeName().equals("Transform")) {
								Transform transform = new Transform();
								for(int l = 0; l < compNode.getChildNodes().getLength(); l++) {
									Node transNode = compNode.getChildNodes().item(l);
									
									if(transNode.getNodeName().equals("Position")) {
										transform.position.x = Float.parseFloat(transNode.getAttributes().getNamedItem("x").getNodeValue());
										transform.position.y = Float.parseFloat(transNode.getAttributes().getNamedItem("y").getNodeValue());
										transform.position.z = Float.parseFloat(transNode.getAttributes().getNamedItem("z").getNodeValue());
									}
									
									if(transNode.getNodeName().equals("Rotation")) {
										transform.rotation.x = Float.parseFloat(transNode.getAttributes().getNamedItem("x").getNodeValue());
										transform.rotation.y = Float.parseFloat(transNode.getAttributes().getNamedItem("y").getNodeValue());
										transform.rotation.z = Float.parseFloat(transNode.getAttributes().getNamedItem("z").getNodeValue());
									}
									
									if(transNode.getNodeName().equals("Scale")) {
										transform.scale.x = Float.parseFloat(transNode.getAttributes().getNamedItem("x").getNodeValue());
										transform.scale.y = Float.parseFloat(transNode.getAttributes().getNamedItem("y").getNodeValue());
										transform.scale.z = Float.parseFloat(transNode.getAttributes().getNamedItem("z").getNodeValue());
									}
								}
								scene.transforms.add(transform);
								entity.addComponent(transform);
							}
							
							if(compNode.getNodeName().equals("Model")) {
								//FIXME item 1
								Node pathNode = compNode.getChildNodes().item(1);
								Path modelpath = new Path(pathNode.getTextContent());
								Model model = AssetLoader.loadModel(modelpath);
								
								if(model.getMeshes().size() > 1) {
									for(Mesh mesh: model.getMeshes()) {
										Entity child = new Entity();
										child.name = mesh.name;
										child.addComponent(new AttachedTo(entity));
										child.addComponent(new Transform(0, 0, 0));
										child.addComponent(mesh);
										
										if(mesh.materialIndex != -1) {
											child.addComponent(new MeshRenderer(model.getMaterials().get(mesh.materialIndex)));
										} else {
											Material mat = new Material("Empty");
											mat.shading = Shading.DIFFUSE;
											MeshRenderer mr = new MeshRenderer(mat);
											
											scene.meshRenderers.add(mr);
											child.addComponent(mr);
										}
										scene.addEntity(child);
									}
								} else {
									Mesh mesh = model.getMeshes().get(0);
									scene.meshes.add(mesh);
									entity.addComponent(mesh);
									if(model.getMaterials().size() > 0) {
										entity.addComponent(new MeshRenderer(model.getMaterials().get(0)));
									} else {
										Material mat = new Material("Empty");
										mat.shading = Shading.DIFFUSE;
										MeshRenderer mr = new MeshRenderer(mat);
										
										//scene.meshRenderers.add(mr);
										//entity.addComponent(mr);
									}
								}
							}
							
							if (compNode.getNodeName().equals("Material")) {
								for (int l = 0; l < compNode.getChildNodes().getLength(); l++) {
									Node matNode = compNode.getChildNodes().item(l);
									
									if (matNode.getNodeName().equals("Path")) {
										Path materialPath = new Path(matNode.getTextContent());
										Material material = AssetLoader.loadMaterials(materialPath).get(0);
										
										if(material != null) {
											MeshRenderer mr = new MeshRenderer(material);
											
											scene.meshRenderers.add(mr);
											entity.addComponent(mr);
										}
									}
									if (matNode.getNodeName().equals("Tiling")) {
										MeshRenderer mr = (MeshRenderer) entity.getComponent(MeshRenderer.class);
										
										float xTiling = Float.parseFloat(matNode.getAttributes().getNamedItem("x").getNodeValue());
										float yTiling = Float.parseFloat(matNode.getAttributes().getNamedItem("y").getNodeValue());
										mr.material.tiling.set(xTiling, yTiling);
									}
								}
							}
							
							if(compNode.getNodeName().equals("Camera")) {
								Camera camera = new Camera();
								camera.setFov(Float.parseFloat(compNode.getAttributes().getNamedItem("fovy").getNodeValue()));
								camera.setAspectRatio(Float.parseFloat(compNode.getAttributes().getNamedItem("aspect").getNodeValue()));
								camera.setZNear(Float.parseFloat(compNode.getAttributes().getNamedItem("zNear").getNodeValue()));
								camera.setZFar(Float.parseFloat(compNode.getAttributes().getNamedItem("zFar").getNodeValue()));
								
								scene.cameras.add(camera);
								entity.addComponent(camera);
								//FIXME allow main camera to be assigned
								scene.mainCamera = entity;
							}
							
							if(compNode.getNodeName().equals("PointLight")) {
								PointLight light = new PointLight();
								
								for(int l = 0; l < compNode.getChildNodes().getLength(); l++) {
									Node lightNode = compNode.getChildNodes().item(l);
									
									if(lightNode.getNodeName().equals("Color")) {
										light.color.x = Float.parseFloat(lightNode.getAttributes().getNamedItem("r").getNodeValue());
										light.color.y = Float.parseFloat(lightNode.getAttributes().getNamedItem("g").getNodeValue());
										light.color.z = Float.parseFloat(lightNode.getAttributes().getNamedItem("b").getNodeValue());
									}
									
									if(lightNode.getNodeName().equals("Attenuation")) {
										light.attenuation.x = Float.parseFloat(lightNode.getAttributes().getNamedItem("c").getNodeValue());
										light.attenuation.y = Float.parseFloat(lightNode.getAttributes().getNamedItem("l").getNodeValue());
										light.attenuation.z = Float.parseFloat(lightNode.getAttributes().getNamedItem("q").getNodeValue());
									}
								}
								
								scene.pointLights.add(light);
								entity.addComponent(light);
							}
							
							if(compNode.getNodeName().equals("DirectionalLight")) {
								DirectionalLight light = new DirectionalLight();
								
								for(int l = 0; l < compNode.getChildNodes().getLength(); l++) {
									Node dLightNode = compNode.getChildNodes().item(l);
									
									if(dLightNode.getNodeName().equals("Color")) {
										light.color.x = Float.parseFloat(dLightNode.getAttributes().getNamedItem("r").getNodeValue());
										light.color.y = Float.parseFloat(dLightNode.getAttributes().getNamedItem("g").getNodeValue());
										light.color.z = Float.parseFloat(dLightNode.getAttributes().getNamedItem("b").getNodeValue());
									}
								}
								
								scene.dirLights.add(light);
								entity.addComponent(light);
							}
							
							if(compNode.getNodeName().equals("MouseLook")) {
								entity.addComponent(new MouseLook());
							}
						}
					}
				}
				scene.addEntity(entity);
			}

			if(entityNode.getNodeName().equals("MainCamera")) {
				//scene.mainCamera = Integer.parseInt(entityNode.getAttributes().getNamedItem("id").getNodeValue());
				//Log.debug("MainCamera: " + scene.mainCamera);
			}
		}
		
		return scene;
	}
}
