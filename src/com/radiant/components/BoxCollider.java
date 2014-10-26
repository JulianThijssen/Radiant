package com.radiant.components;

import com.radiant.entities.Entity;
import com.radiant.util.Vector3f;

public class BoxCollider extends Component {
	public Vector3f center = new Vector3f(0, 0, 0);
	public Vector3f size = new Vector3f(1, 1, 1);
	
	public boolean collidesWith(BoxCollider collider) {
		Entity other = collider.owner;
		
		Transform t1 = (Transform) owner.getComponent(Transform.class);
		Transform t2 = (Transform) other.getComponent(Transform.class);
		
		if(t1 == null || t2 == null) {
			return false;
		}
		
		Vector3f p1 = t1.position;
		Vector3f p2 = t2.position;
		Vector3f s1 = t1.scale;
		Vector3f s2 = t2.scale;
		
		if(p1.x - s1.x > p2.x + s2.x && p1.x + s1.x < p2.x - s2.x &&
		   p1.y - s1.y > p2.y + s2.y && p1.y + s1.y < p2.y - s2.y &&
		   p1.z - s1.z > p2.z + s2.z && p1.z + s1.z < p2.z - s2.z) {
			return true;
		}
		return false;
	}

}
