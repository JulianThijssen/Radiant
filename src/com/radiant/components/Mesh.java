package com.radiant.components;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.geom.Face;

public class Mesh extends Component {
	public String name;
	public ArrayList<Vector3f> vertices = null;
	public ArrayList<Vector2f> textureCoords = null;
	public ArrayList<Vector3f> normals = null;
	public ArrayList<Face> faces = null;
	
	public Mesh(String name) {
		super("Mesh");
		this.name = name;
	}
	
	public void setPlane() {
		vertices = new ArrayList<Vector3f>();
		vertices.add(new Vector3f(-1, 0, -1));
		vertices.add(new Vector3f(-1, 0, 1));
		vertices.add(new Vector3f(1, 0, 1));
		vertices.add(new Vector3f(1, 0, -1));
		
		textureCoords = new ArrayList<Vector2f>();
		textureCoords.add(new Vector2f(0, 1));
		textureCoords.add(new Vector2f(0, 0));
		textureCoords.add(new Vector2f(1, 0));
		textureCoords.add(new Vector2f(1, 1));
		
		normals = new ArrayList<Vector3f>();
		normals.add(new Vector3f(0, 1, 0));
		normals.add(new Vector3f(0, 1, 0));
		normals.add(new Vector3f(0, 1, 0));
		normals.add(new Vector3f(0, 1, 0));
		
		faces = new ArrayList<Face>();
		Face face = new Face();
		face.vi = new int[]{0, 1, 2, 3};
		face.ti = new int[]{0, 1, 2, 3};
		face.ni = new int[]{0, 1, 2, 3};
		faces.add(face);
	}
}
