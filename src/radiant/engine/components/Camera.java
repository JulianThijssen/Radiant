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
	
	public Camera() {
		
	}
	
	public Camera(float left, float right, float bottom, float top, float zNear, float zFar) {
		setOrthographic();
		this.left = left;
		this.right = right;
		this.bottom = bottom;
		this.top = top;
		this.zNear = zNear;
		this.zFar = zFar;
	}
	
	public Camera(float fovy, float aspect, float zNear, float zFar) {
		setPerspective();
		this.fovy = fovy;
		this.aspect = aspect;
		this.zNear = zNear;
		this.zFar = zFar;
	}
	
	public void loadProjectionMatrix(Matrix4f m) {
		recalculate(m);
	}
	
	private void recalculate(Matrix4f m) {
		m.setIdentity();
		if(perspective) {
			m.array[0] = (float) (1.0 / Math.tan(Math.toRadians(fovy / 2.0))) / aspect;
			m.array[5] = (float) (1.0 / Math.tan(Math.toRadians(fovy / 2.0)));
			m.array[10] = (zNear + zFar) / (zNear - zFar);
			m.array[11] = -1;
			m.array[14] = (2 * zNear * zFar) / (zNear - zFar);
			m.array[15] = 0;
		} else {
			m.array[0] = 2 / (right - left);
			m.array[5] = 2 / (top - bottom);
			m.array[10] = -2 / (zFar - zNear);
			m.array[12] = (-right - left) / (right - left);
			m.array[13] = (-top - bottom) / (top - bottom);
			m.array[14] = (-zFar - zNear) / (zFar - zNear);
		}
	}
	
	public void setFov(float fovy) {
		this.fovy = fovy;
	}
	
	public void setAspectRatio(float aspect) {
		this.aspect = aspect;
	}
	
	public void setZNear(float zNear) {
		this.zNear = zNear;
	}
	
	public void setZFar(float zFar) {
		this.zFar = zFar;
	}
	
	public void setPerspective() {
		perspective = true;
	}
	
	public void setOrthographic() {
		perspective = false;
	}
}
