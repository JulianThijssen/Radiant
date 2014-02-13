package com.radiant;

public class Camera {
	public static final float   DEFAULT_FOV = 90;
	public static final float   DEFAULT_ASPECTRATIO = 1;
	public static final float   DEFAULT_ZNEAR = 0.1f;
	public static final float   DEFAULT_ZFAR = 100f;
	public static final boolean DEFAULT_PERSPECTIVE = true;
	public static final float   MIN_PITCH = -90;
	public static final float   MAX_PITCH = 90;
	public static final float   DEFAULT_SENSITIVITY = 0.1f;
	
	public float   fieldOfView = DEFAULT_FOV;
	public float   aspectRatio = DEFAULT_ASPECTRATIO;
	public float   zNear       = DEFAULT_ZNEAR;
	public float   zFar        = DEFAULT_ZFAR;
	public boolean perspective = DEFAULT_PERSPECTIVE;
	public float   sensitivity = DEFAULT_SENSITIVITY;
	
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
}
