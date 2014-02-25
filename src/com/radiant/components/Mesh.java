package com.radiant.components;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import com.radiant.geom.Object;
import com.radiant.material.Material;

public class Mesh extends Component {
	public ArrayList<Object> objects = new ArrayList<Object>();
	public ArrayList<Material> materials = new ArrayList<Material>();
	
	public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
	public ArrayList<Vector2f> textureCoords = new ArrayList<Vector2f>();
	public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
}
