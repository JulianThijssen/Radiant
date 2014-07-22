package com.radiant.components;

import com.radiant.util.Vector3f;

public class Light extends Component {
	public Vector3f color = new Vector3f(1, 1, 1);
	public float constantAtt;
	public float linearAtt;
	public float quadraticAtt;
	
	public Light() {
		super("Light");
	}
}
