package com.radiant.components;

import com.radiant.util.Vector3f;

public class Transform extends Component {
	public Vector3f position = new Vector3f(0, 0, 0);
	public Vector3f rotation = new Vector3f(0, 0, 0);
	public Vector3f scale = new Vector3f(1, 1, 1);
	
	public Transform() {
		super("Transform");
	}
	
	// 3D Transform
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
		
		rotation.x = pitch;
		rotation.y = yaw;
		rotation.z = roll;
		
		scale.x = sx;
		scale.y = sy;
		scale.z = sz;
	}
	
	// 2D Transform
	public Transform(float x, float y) {
		this(x, y, 0, 0, 0, 0, 1, 1, 1);
	}
}
