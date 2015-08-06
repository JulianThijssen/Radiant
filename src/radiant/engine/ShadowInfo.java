package radiant.engine;

import radiant.engine.core.math.Matrix4f;

public class ShadowInfo {
	public int shadowMap;
	public Matrix4f projectionMatrix = new Matrix4f();
	public Matrix4f viewMatrix = new Matrix4f();
	
	public ShadowInfo(int shadowMap) {
		this.shadowMap = shadowMap;
	}
}
