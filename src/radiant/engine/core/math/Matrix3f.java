package radiant.engine.core.math;

import java.nio.FloatBuffer;

public class Matrix3f {
	public float[] array = new float[9];
	
	public Matrix3f() {
		setIdentity();
	}
	
	public Matrix3f(float array[]) {
		for(int i = 0; i < 9; i++) {
			this.array[i] = array[i];
		}
	}

	public void setIdentity() {
		for(int i = 0; i < 9; i++) {
			if(i % 4 == 0) {
				array[i] = 1;
			} else {
				array[i] = 0;
			}
		}
	}
	
	public static Vector3f transform(Matrix3f m, Vector3f v, Vector3f dest) {
		if(dest == null) {
			dest = new Vector3f();
		}
		float x = m.array[0] * v.x + m.array[3] * v.y + m.array[6] * v.z;
		float y = m.array[1] * v.x + m.array[4] * v.y + m.array[7] * v.z;
		float z = m.array[2] * v.x + m.array[5] * v.y + m.array[8] * v.z;
		
		dest.x = x;
		dest.y = y;
		dest.z = z;
		
		return dest;
	}
	
	public void load(FloatBuffer buf) {
		for(int i = 0; i < 9; i++) {
			array[i] = buf.get();
		}
	}
	
	public void store(FloatBuffer buf) {
		for(int i = 0; i < 9; i++) {
			buf.put(array[i]);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(array[0]+" "+array[3]+" "+array[6]+"\n");
		sb.append(array[1]+" "+array[4]+" "+array[7]+"\n");
		sb.append(array[2]+" "+array[5]+" "+array[8]);
		return sb.toString();
	}
}
