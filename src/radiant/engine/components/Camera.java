package radiant.engine.components;

import radiant.engine.core.math.Matrix4f;

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
		recalculate();
	}
	
	public Camera(float left, float right, float bottom, float top, float zNear, float zFar) {
		setOrthographic();
		this.left = left;
		this.right = right;
		this.bottom = bottom;
		this.top = top;
		this.zNear = zNear;
		this.zFar = zFar;
		recalculate();
	}
	
	public Camera(float fovy, float aspect, float zNear, float zFar) {
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
		projectionMatrix.setIdentity();
		if(perspective) {
			projectionMatrix.array[0] = (float) (1 / Math.tan(Math.toRadians(fovy / 2f))) / aspect;
			projectionMatrix.array[5] = (float) (1 / Math.tan(Math.toRadians(fovy / 2f)));
			projectionMatrix.array[10] = (zNear + zFar) / (zNear - zFar);
			projectionMatrix.array[11] = -1;
			projectionMatrix.array[14] = (2 * zNear * zFar) / (zNear - zFar);
			projectionMatrix.array[15] = 0;
		} else {
			projectionMatrix.array[0] = 2 / (right - left);
			projectionMatrix.array[5] = 2 / (top - bottom);
			projectionMatrix.array[10] = -2 / (zFar - zNear);
			projectionMatrix.array[12] = (left - right) / (right - left);
			projectionMatrix.array[13] = (bottom - top) / (top - bottom);
			projectionMatrix.array[14] = (zNear - zFar) / (zFar - zNear);
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
