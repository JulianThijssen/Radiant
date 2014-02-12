package com.radiant;

import org.lwjgl.util.vector.Matrix4f;

public class Camera {
	public static final float   DEFAULT_FOV = 90;
	public static final float   DEFAULT_ASPECTRATIO = 1;
	public static final float   DEFAULT_ZNEAR = 0.1f;
	public static final float   DEFAULT_ZFAR = 100f;
	public static final boolean DEFAULT_PERSPECTIVE = true;
	public static final float   MIN_PITCH = -90;
	public static final float   MAX_PITCH = 90;
	public static final float   DEFAULT_SENSITIVITY = 0.1f;
	
	private float   fieldOfView = DEFAULT_FOV;
	private float   aspectRatio = DEFAULT_ASPECTRATIO;
	private float   zNear       = DEFAULT_ZNEAR;
	private float   zFar        = DEFAULT_ZFAR;
	private boolean perspective = DEFAULT_PERSPECTIVE;
	private float   sensitivity = DEFAULT_SENSITIVITY;
	
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();
	
	public float getFieldOfView() {
		return fieldOfView;
	}
	
	public float getAspectRatio() {
		return aspectRatio;
	}
	
	public float getZNear() {
		return zNear;
	}
	
	public float getZFar() {
		return zFar;
	}
	
	public boolean isOrthogonal() {
		return !perspective;
	}
	
	public float getSensitivity() {
		return sensitivity;
	}
	
	public void setFieldOfView(float fieldOfView) {
		this.fieldOfView = fieldOfView;
	}
	
	public void setAspectRatio(float aspectRatio) {
		this.aspectRatio = aspectRatio;
	}
	
	public void setZNear(float zNear) {
		this.zNear = zNear;
	}
	
	public void setZFar(float zFar) {
		this.zFar = zFar;
	}
	
	public void setPerspective(boolean perspective) {
		this.perspective = perspective;
	}
	
	public void setSensitivity(float sensitivity) {
		this.sensitivity = sensitivity;
	}
	
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	
	public void update(Scene world) {
		
	}
	
	public Matrix4f getProjectionMatrix() {
		projectionMatrix.m00 = (float) (1 / Math.tan(Math.toRadians(fieldOfView / 2f)));
		projectionMatrix.m11 = (float) (1 / Math.tan(Math.toRadians(fieldOfView / 2f)));
		projectionMatrix.m22 = -((zFar + zNear) / (zFar - zNear));
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * zNear * zFar) / (zFar - zNear));
		projectionMatrix.m33 = 0;
		return projectionMatrix;
    }
}
