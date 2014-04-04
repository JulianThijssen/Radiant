package com.radiant.components;

import org.lwjgl.util.vector.Vector3f;

public class Transform extends Component {
	public Vector3f position = new Vector3f(0, 0, 0);
	public Vector3f rotation = new Vector3f(0, 0, 0);
	public Vector3f scale = new Vector3f(1, 1, 1);
	
	public Transform(float x, float y, float z) {
		this(x, y, z, 0, 0, 0, 1, 1, 1);
	}
	
	public Transform(float x, float y, float z, float pitch, float yaw, float roll) {
		this(x, y, z, pitch, yaw, roll, 1, 1, 1);
	}
	
	public Transform(float x, float y, float z, float pitch, float yaw, float roll, float sx, float sy, float sz) {
		super("Transform");
		position.x = x;
		position.y = y;
		position.z = z;
		
		rotation.x = (float) Math.toRadians(pitch);
		rotation.y = (float) Math.toRadians(yaw);
		rotation.z = (float) Math.toRadians(roll);
		
		scale.x = sx;
		scale.y = sy;
		scale.z = sz;
	}
}
