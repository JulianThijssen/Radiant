package com.radiant.components;

import org.lwjgl.util.vector.Vector3f;

public class Body extends Component {
	public Vector3f velocity = new Vector3f(0, 0, 0);
	public boolean useGravity = true;
	
	public Body() {
		super("Body");
	}
}
