package com.radiant.components;

import org.lwjgl.util.vector.Vector3f;

public class Transform extends Component {
	public Vector3f position = new Vector3f(0, 0, 0);
	public Vector3f rotation = new Vector3f(0, 0, 0);
	public Vector3f scale = new Vector3f(1, 1, 1);
	
	public Transform(float x, float y, float z) {
		position.x = x;
		position.y = y;
		position.z = z;
	}
	
	public Transform(float x, float y, float z, float pitch, float yaw, float roll) {
		position.x = x;
		position.y = y;
		position.z = z;
		
		rotation.x = pitch;
		rotation.y = yaw;
		rotation.z = roll;
	}
	
	public Transform(float x, float y, float z, float pitch, float yaw, float roll, float sx, float sy, float sz) {
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
}
