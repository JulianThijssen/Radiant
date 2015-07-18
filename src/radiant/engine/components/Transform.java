package radiant.engine.components;

import radiant.engine.core.math.Vector3f;

public class Transform extends Component {
	public Vector3f position = new Vector3f(0, 0, 0);
	public Vector3f rotation = new Vector3f(0, 0, 0);
	public Vector3f scale = new Vector3f(1, 1, 1);
	
	public Transform() {
		
	}
	
	// 3D Transform
	public Transform(float x, float y, float z) {
		this(x, y, z, 0, 0, 0, 1, 1, 1);
	}
	
	public Transform(float x, float y, float z, float pitch, float yaw, float roll) {
		this(x, y, z, pitch, yaw, roll, 1, 1, 1);
	}
	
	public Transform(float x, float y, float z, float pitch, float yaw, float roll, float sx, float sy, float sz) {
		position.set(x, y, z);
		rotation.set(pitch, yaw, roll);
		scale.set(sx, sy, sz);
	}
	
	// 2D Transform
	public Transform(float x, float y) {
		this(x, y, 0, 0, 0, 0, 1, 1, 1);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Transform Component\n");
		sb.append(String.format("Position: <%f, %f %f>\n", position.x, position.y, position.z));
		sb.append(String.format("Rotation: <%f, %f %f>\n", rotation.x, rotation.y, rotation.z));
		sb.append(String.format("Scale: <%f, %f %f>\n", scale.x, scale.y, scale.z));
		return sb.toString();
	}
}
