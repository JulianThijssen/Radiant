package radiant.engine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.List;

import radiant.assets.AssetLoader;
import radiant.assets.material.Material;
import radiant.assets.material.Shading;
import radiant.assets.shader.Shader;
import radiant.assets.texture.TextureData;
import radiant.assets.texture.TextureLoader;
import radiant.engine.components.AttachedTo;
import radiant.engine.components.Camera;
import radiant.engine.components.DirectionalLight;
import radiant.engine.components.Mesh;
import radiant.engine.components.MeshRenderer;
import radiant.engine.components.PointLight;
import radiant.engine.components.ReflectionProbe;
import radiant.engine.components.Transform;
import radiant.engine.core.diag.Clock;
import radiant.engine.core.file.Path;
import radiant.engine.core.math.Matrix4f;
import radiant.engine.core.math.Vector3f;

public class ForwardRenderer extends Renderer {
	private FrameBuffer shadowBuffer;
	private FrameBuffer reflBuffer;
	
	private int drawCalls = 0;
	private Clock clock = new Clock();
	
	@Override
	public void create() {
		setGlParameters();
		loadShaders();
	}
	
	@Override
	public void destroy() {
		
	}
	
	/**
	 * Sets the basic OpenGL parameters concerning back face culling,
	 * texture wrapping and alpha handling.
	 */
	private void setGlParameters() {
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
		
		shadowBuffer = new FrameBuffer();
		reflBuffer = new FrameBuffer();
	}
	
	/**
	 * Initialise all the shader buckets
	 */
	private void loadShaders() {
		shaders.put(Shading.NONE, null);
		shaders.put(Shading.UNSHADED, AssetLoader.loadShader(new Path("shaders/unshaded")));
		shaders.put(Shading.DIFFUSE, AssetLoader.loadShader(new Path("shaders/diffuse")));
		shaders.put(Shading.NORMAL, AssetLoader.loadShader(new Path("shaders/normal")));
		shaders.put(Shading.SPECULAR, AssetLoader.loadShader(new Path("shaders/specular")));
		shaders.put(Shading.SHADOW, AssetLoader.loadShader(new Path("shaders/shadow")));
		shaders.put(Shading.TEXTURE, AssetLoader.loadShader(new Path("shaders/texture")));
		shaders.put(Shading.REFLECTIVE, AssetLoader.loadShader(new Path("shaders/reflective")));
		shaders.put(Shading.DEBUG, AssetLoader.loadShader(new Path("shaders/debug")));
		
		for(Shader shader: shaders.values()) {
			shaderMap.put(shader, new ArrayList<Entity>());
		}
	}
	
	/**
	 * Renders the current complete scene graph
	 */
	@Override
	public void update() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		// Reset the draw calls before the next render
		drawCalls = 0;
		
		// If there is no main camera in the scene, nothing can be rendered
		if(scene.mainCamera == null) {
			return;
		}
		
		clock.start();
		
		// Divide the meshes into shader buckets
		divideMeshes();
		
		Camera camera = scene.mainCamera.getComponent(Camera.class);
		Transform ct = scene.mainCamera.getComponent(Transform.class);
		
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_BLEND);
		
		// Generate the shadow maps
		genShadowMaps();
		
		genReflectionMaps();
		
		// Set the viewport to the normal window size
		glViewport(0, 0, Window.width, Window.height);
		
		render(camera, ct);
	}
	
	private void render(Camera camera, Transform t) {
		// Set the clear color
		glClearColor(clearColor.x, clearColor.y, clearColor.z, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		camera.loadProjectionMatrix(projectionMatrix);
		// Calculate view matrix
		viewMatrix.setIdentity();
		viewMatrix.rotate(Vector3f.negate(t.rotation));
		viewMatrix.translate(Vector3f.negate(t.position));
		
		// Render all the meshes associated with a shader
		
		// Multiply diffuse texture with the lighting
		Shader shader = shaders.get(Shading.TEXTURE);
		glUseProgram(shader.handle);

		glUniform1f(glGetUniformLocation(shader.handle, "ambientLight"), scene.ambient);
		
		renderScene(shader, t, camera);
		
		// Enable the blending of light contributions
		glEnable(GL_BLEND);
		glBlendFuncSeparate(GL_ONE, GL_ONE, GL_ONE, GL_ZERO);
		glDepthFunc(GL_LEQUAL);
		
		// Unshaded
		shader = shaders.get(Shading.UNSHADED);
		glUseProgram(shader.handle);
		
		glUniformMatrix4(shader.uniform("projectionMatrix"), false, projectionMatrix.getBuffer());
		glUniformMatrix4(shader.uniform("viewMatrix"), false, viewMatrix.getBuffer());
		
		for(Entity entity: shaderMap.get(shader)) {
			drawMesh(shader, entity);
		}
		
//		for (PointLight point: scene.pointLights) {
//			drawMesh()
//		}
		
		// Diffuse
		shader = shaders.get(Shading.DIFFUSE);
		glUseProgram(shader.handle);
		
		glUniformMatrix4(shader.uniform("projectionMatrix"), false, projectionMatrix.getBuffer());
		glUniformMatrix4(shader.uniform("viewMatrix"), false, viewMatrix.getBuffer());
		
		for (PointLight light: scene.pointLights) {
			uploadPointLight(shader, light);
			
			for(Entity entity: shaderMap.get(shader)) {				
				drawMesh(shader, entity);
			}
		}
		for (DirectionalLight light: scene.dirLights) {
			uploadDirectionalLight(shader, light);
			
			for(Entity entity: shaderMap.get(shader)) {				
				drawMesh(shader, entity);
			}
		}
		
		// Normal
		shader = shaders.get(Shading.NORMAL);
		glUseProgram(shader.handle);
		
		glUniformMatrix4(shader.uniform("projectionMatrix"), false, projectionMatrix.getBuffer());
		glUniformMatrix4(shader.uniform("viewMatrix"), false, viewMatrix.getBuffer());
		
		for (PointLight light: scene.pointLights) {
			uploadPointLight(shader, light);
			
			for(Entity entity: shaderMap.get(shader)) {				
				drawMesh(shader, entity);
			}
		}
		for (DirectionalLight light: scene.dirLights) {
			uploadDirectionalLight(shader, light);
			
			for(Entity entity: shaderMap.get(shader)) {				
				drawMesh(shader, entity);
			}
		}
		
		// Specular
		shader = shaders.get(Shading.SPECULAR);
		glUseProgram(shader.handle);
		
		glUniform3f(shader.uniform("cameraPosition"), t.position.x, t.position.y, t.position.z);
		glUniformMatrix4(shader.uniform("projectionMatrix"), false, projectionMatrix.getBuffer());
		glUniformMatrix4(shader.uniform("viewMatrix"), false, viewMatrix.getBuffer());
		glUniform1i(glGetUniformLocation(shader.handle, "reflections"), 1);
		
		for (PointLight light: scene.pointLights) {
			uploadPointLight(shader, light);
			
			for(Entity entity: shaderMap.get(shader)) {
				drawMesh(shader, entity);
			}
		}
		for (DirectionalLight light: scene.dirLights) {
			uploadDirectionalLight(shader, light);
			
			for(Entity entity: shaderMap.get(shader)) {				
				drawMesh(shader, entity);
			}
		}
		
		// Reflective
		shader = shaders.get(Shading.REFLECTIVE);
		glUseProgram(shader.handle);
		
		glUniform3f(shader.uniform("cameraPosition"), t.position.x, t.position.y, t.position.z);
		glUniformMatrix4(shader.uniform("projectionMatrix"), false, projectionMatrix.getBuffer());
		glUniformMatrix4(shader.uniform("viewMatrix"), false, viewMatrix.getBuffer());
		
		for (ReflectionProbe probe: scene.probes) {
			glActiveTexture(GL_TEXTURE6);
			glBindTexture(GL_TEXTURE_CUBE_MAP, probe.cubeMap.colorMap);
			glUniform1i(glGetUniformLocation(shader.handle, "reflCubeMap"), 6);
			
			for (PointLight light: scene.pointLights) {
				uploadPointLight(shader, light);
				
				for(Entity entity: shaderMap.get(shader)) {
					drawMesh(shader, entity);
				}
			}
			for (DirectionalLight light: scene.dirLights) {
				uploadDirectionalLight(shader, light);
				
				for(Entity entity: shaderMap.get(shader)) {
					drawMesh(shader, entity);
				}
			}
		}
		
		clock.end();

		glDisable(GL_BLEND);
		
		//System.out.println("Total: " + clock.getNanoseconds());
	}
	
	@Override
	protected void renderScene(Shader shader, Transform transform, Camera camera) {
		Matrix4f projectionMatrix = new Matrix4f();
		Matrix4f viewMatrix = new Matrix4f();
		
		camera.loadProjectionMatrix(projectionMatrix);
		// Calculate view matrix
		viewMatrix.setIdentity();
		viewMatrix.rotate(Vector3f.negate(transform.rotation));
		viewMatrix.translate(Vector3f.negate(transform.position));
		
		glUseProgram(shader.handle);
		
		glUniformMatrix4(shader.uniform("projectionMatrix"), false, projectionMatrix.getBuffer());
		glUniformMatrix4(shader.uniform("viewMatrix"), false, viewMatrix.getBuffer());
		
		for(Entity entity: scene.getEntities()) {
			Mesh mesh = entity.getComponent(Mesh.class);
			
			if(mesh != null) {
				drawMesh(shader, entity);
			}
		}
	}
	
	private void genShadowMaps() {
		// Generate the shadow maps
		for (DirectionalLight light: scene.dirLights) {
			Transform lightT = light.owner.getComponent(Transform.class);
			// TODO Make convex hull of scene to get parameters
			Camera lightC = new Camera(-20, 20, -20, 20, -30, 30);
			lightC.loadProjectionMatrix(light.shadowInfo.projectionMatrix);
			light.shadowInfo.viewMatrix.setIdentity();
			light.shadowInfo.viewMatrix.rotate(Vector3f.negate(lightT.rotation));
			light.shadowInfo.viewMatrix.translate(Vector3f.negate(lightT.position));
			
			if (light.shadowInfo != null) {
				int resolution = light.shadowInfo.resolution;
				
				// Set the viewport to the size of the shadow map
				glViewport(0, 0, resolution, resolution);
				
				// Set the shadow shader to render the shadow map with
				Shader shader = shaders.get(Shading.SHADOW);
				glUseProgram(shader.handle);
				
				// Set up the framebuffer and validate it
				shadowBuffer.bind();
				shadowBuffer.setTexture(GL_DEPTH_ATTACHMENT, light.shadowInfo.shadowMap);
				shadowBuffer.disableColor();
				shadowBuffer.validate();
				
				// Clear the framebuffer and render the scene from the view of the light
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				
				renderScene(shader, lightT, lightC);
			}
		}
		for (PointLight light: scene.pointLights) {
			Transform lightT = light.owner.getComponent(Transform.class);
			Camera lightC = new Camera(90, 1, 0.1f, 20);
			
			for (int i = 0; i < 6; i++) {
				lightT.rotation = CubeMap.transforms[i];

				// Set the viewport to the size of the shadow map
				glViewport(0, 0, light.shadowMap.getResolution(), light.shadowMap.getResolution());
				
				// Set the shadow shader to render the shadow map with
				Shader shader = shaders.get(Shading.SHADOW);
				glUseProgram(shader.handle);
				
				// Set up the framebuffer and validate it
				shadowBuffer.bind();
				shadowBuffer.setDepthCubeMap(light.shadowMap, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
				shadowBuffer.disableColor();
				shadowBuffer.validate();

				// Upload the light matrices
				glUniform3f(shader.uniform("lightPos"), lightT.position.x, lightT.position.y, lightT.position.z); 
				
				// Set the clear color to be the furthest distance possible
				shadowBuffer.setClearColor(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, 1);
				
				// Clear the framebuffer and render the scene from the view of the light
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

				renderScene(shaders.get(Shading.SHADOW), lightT, lightC);
			}
		}
		shadowBuffer.unbind();
	}
	
	private void genReflectionMaps() {
		for (Entity e: scene.getEntities()) {
			MeshRenderer mr = e.getComponent(MeshRenderer.class);
			Camera cam = scene.mainCamera.getComponent(Camera.class);
			Transform ct = scene.mainCamera.getComponent(Transform.class);
			
			if (mr != null && mr.material.reflective) {
				reflBuffer.bind();
				reflBuffer.setTexture(GL_COLOR_ATTACHMENT0, mr.material.reflectionMap);
				int depthMap = TextureLoader.create(GL_DEPTH24_STENCIL8, Window.width, Window.height, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, null);
				reflBuffer.setTexture(GL_DEPTH_STENCIL_ATTACHMENT, depthMap);
				reflBuffer.enableColor(GL_COLOR_ATTACHMENT0);
				reflBuffer.validate();
				
				Shader shader = shaders.get(Shading.UNSHADED);
				glUseProgram(shader.handle);
				
				glEnable(GL_STENCIL_TEST);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
				
				glStencilFunc(GL_NEVER, 1, 1);
				glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
				
				cam.loadProjectionMatrix(projectionMatrix);
				// Calculate view matrix
				viewMatrix.setIdentity();
				viewMatrix.rotate(Vector3f.negate(ct.rotation));
				viewMatrix.translate(Vector3f.negate(ct.position));
				
				glUniformMatrix4(shader.uniform("projectionMatrix"), false, projectionMatrix.getBuffer());
				glUniformMatrix4(shader.uniform("viewMatrix"), false, viewMatrix.getBuffer());
				
				glViewport(0, 0, Window.width, Window.height);
				glColorMask(false, false, false, false);
				glDepthMask(false);
				drawMesh(shader, e);
				glDepthMask(true);
				glColorMask(true, true, true, true);
				
				glStencilFunc(GL_EQUAL, 1, 1);
				glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

				glDisable(GL_BLEND);
				ct.scale.y *= -1;
				
				////////////////////////////////
				// Set the clear color
				glClearColor(clearColor.x, clearColor.y, clearColor.z, 1);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				
				cam.loadProjectionMatrix(projectionMatrix);
				// Calculate view matrix
				viewMatrix.setIdentity();
				viewMatrix.rotate(Vector3f.negate(ct.rotation));
				viewMatrix.translate(Vector3f.negate(ct.position));
				viewMatrix.scale(new Vector3f(1, -1, 1));

				// Render all the meshes associated with a shader
				Matrix4f biasMatrix = new Matrix4f();
				biasMatrix.array[0] = 0.5f;
				biasMatrix.array[5] = 0.5f;
				biasMatrix.array[10] = 0.5f;
				biasMatrix.array[12] = 0.5f;
				biasMatrix.array[13] = 0.5f;
				biasMatrix.array[14] = 0.5f;
				
				glCullFace(GL_FRONT);
				
				// Multiply diffuse texture with the lighting
				shader = shaders.get(Shading.TEXTURE);
				glUseProgram(shader.handle);

				glUniform1f(glGetUniformLocation(shader.handle, "ambientLight"), scene.ambient);
				
				glUseProgram(shader.handle);
				
				glUniformMatrix4(shader.uniform("projectionMatrix"), false, projectionMatrix.getBuffer());
				glUniformMatrix4(shader.uniform("viewMatrix"), false, viewMatrix.getBuffer());
				
				for(Entity entity: scene.getEntities()) {
					Mesh mesh = entity.getComponent(Mesh.class);
					
					if(mesh != null) {
						drawMesh(shader, entity);
					}
				}
				
				// Enable the blending of light contributions
				glEnable(GL_BLEND);
				glBlendFuncSeparate(GL_ONE, GL_ONE, GL_ONE, GL_ZERO);
				glDepthFunc(GL_LEQUAL);

				// Specular
				shader = shaders.get(Shading.SPECULAR);
				glUseProgram(shader.handle);
				
				glUniformMatrix4(shader.uniform("biasMatrix"), false, biasMatrix.getBuffer());
				glUniform3f(shader.uniform("cameraPosition"), ct.position.x, ct.position.y, ct.position.z);
				glUniformMatrix4(shader.uniform("projectionMatrix"), false, projectionMatrix.getBuffer());
				glUniformMatrix4(shader.uniform("viewMatrix"), false, viewMatrix.getBuffer());
				glUniform1i(glGetUniformLocation(shader.handle, "reflections"), 0);
				
				for (PointLight light: scene.pointLights) {
					uploadPointLight(shader, light);
					
					for(Entity entity: shaderMap.get(shader)) {
						drawMesh(shader, entity);
					}
				}
				for (DirectionalLight light: scene.dirLights) {
					uploadDirectionalLight(shader, light);
					
					for(Entity entity: shaderMap.get(shader)) {				
						drawMesh(shader, entity);
					}
				}

				glCullFace(GL_BACK);
				glDisable(GL_BLEND);
				////////////////////////////////

				ct.scale.y *= -1;
				
				glDisable(GL_STENCIL_TEST);
				
				glDeleteTextures(depthMap);
				
				reflBuffer.unbind();
			}
		}
		for (ReflectionProbe probe: scene.probes) {
			Camera camera  = new Camera(90, 1, 0.1f, 20);
			Vector3f probePos = probe.owner.getComponent(Transform.class).position;
			Transform transform = new Transform(probePos.x, probePos.y, probePos.z);
			
			for (int i = 0; i < 6; i++) {
				transform.rotation = CubeMap.transforms[i];
				
				glViewport(0, 0, probe.getResolution(), probe.getResolution());
				
				reflBuffer.bind();
				reflBuffer.setCubeMap(probe.cubeMap, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
				reflBuffer.enableColor(GL_COLOR_ATTACHMENT0);
				reflBuffer.validate();
				
				render(camera, transform);
				
				reflBuffer.unbind();
			}
		}
	}
	
	/**
	 * Divide the meshes in the scene into their appropriate shader buckets
	 */
	private void divideMeshes() {
		for(List<Entity> meshes: shaderMap.values()) {
			meshes.clear();
		}
		for(Entity e: scene.getEntities()) {
			Mesh mesh = e.getComponent(Mesh.class);
			MeshRenderer mr = e.getComponent(MeshRenderer.class);
			if(mesh == null || mr == null) {
				continue;
			}
			
			Shader shader = shaders.get(mr.material.shading);
			shaderMap.get(shader).add(e);
		}
	}

	/**
	 * Uploads a point light to the shader
	 * @param shader The shader currently in use
	 * @param lights The point light to upload
	 */
	private void uploadPointLight(Shader shader, PointLight light) {
		Entity e = light.owner;
		Transform lightT = e.getComponent(Transform.class);
		
		glActiveTexture(GL_TEXTURE4);
		glBindTexture(GL_TEXTURE_CUBE_MAP, light.shadowMap.depthMap);
		glUniform1i(shader.uniform("shadowCubeMap"), 4);

		glUniform1i(shader.uniform("isPointLight"), 1);
		glUniform1i(shader.uniform("isDirLight"), 0);
		glUniform3f(shader.uniform("pointLight.position"), lightT.position.x, lightT.position.y, lightT.position.z);
		glUniform3f(shader.uniform("pointLight.color"), light.color.x, light.color.y, light.color.z);
		glUniform1f(shader.uniform("pointLight.energy"), light.energy);
		glUniform1f(shader.uniform("pointLight.distance"), light.distance);
		if (light.castShadows) {
			glUniform1i(shader.uniform("pointLight.castShadows"), 1);
		} else {
			glUniform1i(shader.uniform("pointLight.castShadows"), 0);
		}
	}
	
	/**
	 * Uploads a directional light to the shader
	 * @param shader The shader currently in use
	 * @param lights The directional light to upload
	 */
	private void uploadDirectionalLight(Shader shader, DirectionalLight light) {
		Entity e = light.owner;
		Transform lightT = e.getComponent(Transform.class);
		
		Matrix4f m = new Matrix4f();
		m.rotate(lightT.rotation);
		Vector3f dir = new Vector3f(0, 0, -1);
		dir = m.transform(dir, 0);
		
		glActiveTexture(GL_TEXTURE5);
		ShadowInfo shadowInfo = light.shadowInfo;
		glBindTexture(GL_TEXTURE_2D, shadowInfo.shadowMap);
		glUniform1i(shader.uniform("shadowInfo.shadowMap"), 5);
		glUniformMatrix4(shader.uniform("shadowInfo.projectionMatrix"), false, shadowInfo.projectionMatrix.getBuffer());
		glUniformMatrix4(shader.uniform("shadowInfo.viewMatrix"), false, shadowInfo.viewMatrix.getBuffer());
		
		glUniform1i(shader.uniform("isPointLight"), 0);
		glUniform1i(shader.uniform("isDirLight"), 1);
		glUniform3f(shader.uniform("dirLight.direction"), dir.x, dir.y, dir.z);
		glUniform3f(shader.uniform("dirLight.color"), light.color.x, light.color.y, light.color.z);
		glUniform1f(shader.uniform("dirLight.energy"), light.energy);
		if (light.castShadows) {
			glUniform1i(shader.uniform("dirLight.castShadows"), 1);
		} else {
			glUniform1i(shader.uniform("dirLight.castShadows"), 0);
		}
	}
	
	/**
	 * Uploads the specified material to the shaders
	 * @param shader The shader currently in use
	 * @param mat    The material to be uploaded
	 */
	private void uploadMaterial(Shader shader, Material mat) {
		// Colors
		glUniform3f(shader.uniform("material.diffuseColor"),	mat.diffuseColor.x, mat.diffuseColor.y, mat.diffuseColor.z);
		glUniform3f(shader.uniform("material.specularColor"), mat.specularColor.x, mat.specularColor.y, mat.specularColor.z);
		
		glUniform1f(shader.uniform("material.specularIntensity"), mat.specularIntensity);
		glUniform2f(shader.uniform("material.tiling"), mat.tiling.x, mat.tiling.y);
		glUniform1f(shader.uniform("material.hardness"), mat.hardness);
		
		if(mat.receiveShadows) {
			glUniform1i(shader.uniform("material.receiveShadows"), 1);
		} else {
			glUniform1i(shader.uniform("material.receiveShadows"), 0);
		}
		
		// Diffuse texture
		if(mat.diffuseMap != null) {
			TextureData diffuseMap = AssetLoader.loadTexture(mat.diffuseMap);

			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, diffuseMap.handle);
			glUniform1i(shader.uniform("material.diffuseMap"), 0);
			
			// Let the shader know we uploaded a diffuse map
			glUniform1i(shader.uniform("material.hasDiffuseMap"), 1);
		} else {
			glUniform1i(shader.uniform("material.hasDiffuseMap"), 0);
		}
		// Normal texture
		if(mat.normalMap != null) {
			TextureData normalMap = AssetLoader.loadTexture(mat.normalMap);

			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, normalMap.handle);
			glUniform1i(shader.uniform("material.normalMap"), 1);
			
			// Let the shader know we uploaded a normal map
			glUniform1i(shader.uniform("material.hasNormalMap"), 1);
		} else {
			glUniform1i(shader.uniform("material.hasNormalMap"), 0);
		}
		// Specular texture
		if(mat.specularMap != null) {
			TextureData specularMap = AssetLoader.loadTexture(mat.specularMap);

			glActiveTexture(GL_TEXTURE2);
			glBindTexture(GL_TEXTURE_2D, specularMap.handle);
			glUniform1i(shader.uniform("material.specularMap"), 2);

			// Let the shader know we uploaded a specular map
			glUniform1i(shader.uniform("material.hasSpecularMap"), 1);
		} else {
			glUniform1i(shader.uniform("material.hasSpecularMap"), 0);
		}
		// Reflection texture
		if (mat.reflective) {
			glActiveTexture(GL_TEXTURE3);
			glBindTexture(GL_TEXTURE_2D, mat.reflectionMap);
			glUniform1i(shader.uniform("material.reflectionMap"), 3);
			
			glUniform1i(shader.uniform("material.hasReflectionMap"), 1);
		} else {
			glUniform1i(shader.uniform("material.hasReflectionMap"), 0);
		}
	}
	
	/**
	 * Draws the mesh associated with the given entity
	 * @param shader The shader currently in use
	 * @param entity The entity that has the mesh component to be drawn
	 */
	@Override
	protected void drawMesh(Shader shader, Entity entity) {
		Transform transform = entity.getComponent(Transform.class);
		Mesh mesh           = entity.getComponent(Mesh.class);
		MeshRenderer mr     = entity.getComponent(MeshRenderer.class);
		AttachedTo attached = entity.getComponent(AttachedTo.class);
		
		if(transform == null) {
			return;
		}
		
		modelMatrix.setIdentity();
		
		// Go up the hierarchy and stack transformations if this entity has a parent
		if(attached != null) {
			Entity parent = attached.parent;
			Transform parentT = parent.getComponent(Transform.class);

			modelMatrix.translate(parentT.position);
			modelMatrix.rotate(parentT.rotation);
			//modelMatrix.scale(parentT.scale); //FIXME allow scaling
		}
		
		modelMatrix.translate(transform.position);
		modelMatrix.rotate(transform.rotation);
		modelMatrix.scale(transform.scale);
		
		// Upload matrices to the shader
		glUniformMatrix4(shader.uniform("modelMatrix"), false, modelMatrix.getBuffer());
		
		if(mr.material != null) {
			uploadMaterial(shader, mr.material);
		}
		
		glBindVertexArray(mesh.handle);
		glDrawArrays(GL_TRIANGLES, 0, mesh.getNumFaces() * 3);
		glBindVertexArray(0);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, 0);
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, 0);
		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, 0);
		glActiveTexture(GL_TEXTURE3);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		// Add a drawcall to the counter
		drawCalls++;
	}
}
