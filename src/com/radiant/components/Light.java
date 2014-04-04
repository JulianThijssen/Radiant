package com.radiant.components;

import org.lwjgl.util.vector.Vector3f;

public class Light extends Component {
	public Vector3f color = new Vector3f(1, 1, 1);
	
	public Light() {
		super("Light");
	}
}
