package com.radiant.components;

import org.lwjgl.util.vector.Matrix4f;

public class Camera extends Component {
	public static final float   DEFAULT_FOVY = 90;
	public static final float   DEFAULT_ASPECTRATIO = 1;
	public static final float   DEFAULT_LEFT = -1;
	public static final float   DEFAULT_RIGHT = 1;
	public static final float   DEFAULT_TOP = 1;
	public static final float   DEFAULT_BOTTOM = -1;
	public static final float   DEFAULT_ZNEAR = 0.1f;
	public static final float   DEFAULT_ZFAR = 100f;
	public static final boolean DEFAULT_PERSPECTIVE = true;
	
	private float   fovy        = DEFAULT_FOVY;
	private float   aspect      = DEFAULT_ASPECTRATIO;
	private float   left        = DEFAULT_LEFT;
	private float   right       = DEFAULT_RIGHT;
	private float   top         = DEFAULT_TOP;
	private float   bottom      = DEFAULT_BOTTOM;
	private float   zNear       = DEFAULT_ZNEAR;
	private float   zFar        = DEFAULT_ZFAR;
	private boolean perspective = DEFAULT_PERSPECTIVE;
	
	private Matrix4f projectionMatrix = new Matrix4f();
	
	public Camera() {
		super("Camera");
		recalculate();
	}
	
	public Camera(float left, float right, float top, float bottom, float zNear, float zFar) {
		super("Camera");
		setOrthographic();
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.zNear = zNear;
		this.zFar = zFar;
		recalculate();
	}
	
	public Camera(float fovy, float aspect, float zNear, float zFar) {
		super("Camera");
		setPerspective();
		this.fovy = fovy;
		this.aspect = aspect;
		this.zNear = zNear;
		this.zFar = zFar;
		recalculate();
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
	
	private void recalculate() {
		if(perspective) {
			projectionMatrix.m00 = (float) (1 / Math.tan(Math.toRadians(fovy / 2f))) / aspect;
			projectionMatrix.m11 = (float) (1 / Math.tan(Math.toRadians(fovy / 2f)));
			projectionMatrix.m22 = (zNear + zFar) / (zNear - zFar);
			projectionMatrix.m23 = -1;
			projectionMatrix.m32 = (2 * zNear * zFar) / (zNear - zFar);
			projectionMatrix.m33 = 0;
		} else {
			projectionMatrix.m00 = 2 / (right - left);
			projectionMatrix.m11 = 2 / (top - bottom);
			projectionMatrix.m22 = -2 / (zFar - zNear);
			projectionMatrix.m30 = (left - right) / (right - left);
			projectionMatrix.m31 = (bottom - top) / (top - bottom);
			projectionMatrix.m32 = (zNear - zFar) / (zFar - zNear);
		}
	}
	
	public void setFov(float fovy) {
		this.fovy = fovy;
		recalculate();
	}
	
	public void setAspectRatio(float aspect) {
		this.aspect = aspect;
		recalculate();
	}
	
	public void setZNear(float zNear) {
		this.zNear = zNear;
		recalculate();
	}
	
	public void setZFar(float zFar) {
		this.zFar = zFar;
		recalculate();
	}
	
	public void setPerspective() {
		perspective = true;
		recalculate();
	}
	
	public void setOrthographic() {
		perspective = false;
		recalculate();
	}
}
