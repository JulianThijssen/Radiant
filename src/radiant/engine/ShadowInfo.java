package radiant.engine;

import radiant.engine.core.math.Matrix4f;

public class ShadowInfo {
	public int shadowMap;
	public Matrix4f projectionMatrix;
	public Matrix4f viewMatrix;
	
	public ShadowInfo(int shadowMap, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
		this.shadowMap = shadowMap;
		this.projectionMatrix = projectionMatrix;
		this.viewMatrix = viewMatrix;
	}
}
