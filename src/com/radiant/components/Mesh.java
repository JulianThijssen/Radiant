package com.radiant.components;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.assets.MaterialLibrary;
import com.radiant.assets.Object;

public class Mesh extends Component {
	public ArrayList<Object> objects = new ArrayList<Object>();
	
	public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
	public ArrayList<Vector2f> textureCoords = new ArrayList<Vector2f>();
	public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
	
	public MaterialLibrary materials = new MaterialLibrary();
	
	public Mesh() {
		super("Mesh");
	}
}
