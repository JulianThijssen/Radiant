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
}
