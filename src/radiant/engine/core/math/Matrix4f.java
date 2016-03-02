package radiant.engine.core.math;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class Matrix4f {
	public float[] array = new float[16];
	private FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
	
	public Matrix4f() {
		setIdentity();
	}
	
	public Matrix4f(float array[]) {
		for(int i = 0; i < 16; i++) {
			this.array[i] = array[i];
		}
	}

	public void setIdentity() {
		for(int i = 0; i < 16; i++) {
			if(i % 5 == 0) {
				array[i] = 1;
			} else {
				array[i] = 0;
			}
		}
	}
	
	public FloatBuffer getBuffer() {
		for(int i = 0; i < 16; i++) {
			buffer.put(array[i]);
		}
		buffer.flip();
		
		return buffer;
	}
	
	public void translate(Vector3f v) {
		array[12] += array[0] * v.x + array[4] * v.y + array[8] * v.z;
		array[13] += array[1] * v.x + array[5] * v.y + array[9] * v.z;
		array[14] += array[2] * v.x + array[6] * v.y + array[10] * v.z;
		array[15] += array[3] * v.x + array[7] * v.y + array[11] * v.z;
	}

	public void rotate(float angle, float x, float y, float z) {
		float c = (float) Math.cos(angle * Math.PI / 180);
		float s = (float) Math.sin(angle * Math.PI / 180);
		float ic = 1 - c;
		
		float f0 = array[0] * ((x * x * ic) + c) + array[4] * ((x * y * ic) + (z * s)) + array[8] * ((x * z * ic) - (y * s));
		float f1 = array[1] * ((x * x * ic) + c) + array[5] * ((x * y * ic) + (z * s)) + array[9] * ((x * z * ic) - (y * s));
		float f2 = array[2] * ((x * x * ic) + c) + array[6] * ((x * y * ic) + (z * s)) + array[10] * ((x * z * ic) - (y * s));
		float f3 = array[3] * ((x * x * ic) + c) + array[7] * ((x * y * ic) + (z * s)) + array[11] * ((x * z * ic) - (y * s));
		float f4 = array[0] * ((x * y * ic) - (z * s)) + array[4] * ((y * y * ic) + c) + array[8] * ((y * z * ic) + (x * s));
		float f5 = array[1] * ((x * y * ic) - (z * s)) + array[5] * ((y * y * ic) + c) + array[9] * ((y * z * ic) + (x * s));
		float f6 = array[2] * ((x * y * ic) - (z * s)) + array[6] * ((y * y * ic) + c) + array[10] * ((y * z * ic) + (x * s));
		float f7 = array[3] * ((x * y * ic) - (z * s)) + array[7] * ((y * y * ic) + c) + array[11] * ((y * z * ic) + (x * s));
		float f8 = array[0] * ((x * z * ic) + (y * s)) + array[4] * ((y * z * ic) - (x * s)) + array[8] * ((z * z * ic) + c);
		float f9 = array[1] * ((x * z * ic) + (y * s)) + array[5] * ((y * z * ic) - (x * s)) + array[9] * ((z * z * ic) + c);
		float f10 = array[2] * ((x * z * ic) + (y * s)) + array[6] * ((y * z * ic) - (x * s)) + array[10] * ((z * z * ic) + c);
		float f11 = array[3] * ((x * z * ic) + (y * s)) + array[7] * ((y * z * ic) - (x * s)) + array[11] * ((z * z * ic) + c);
		
		array[0] = f0;
		array[1] = f1;
		array[2] = f2;
		array[3] = f3;
		array[4] = f4;
		array[5] = f5;
		array[6] = f6;
		array[7] = f7;
		array[8] = f8;
		array[9] = f9;
		array[10] = f10;
		array[11] = f11;
	}
	
	public void rotate(Vector3f euler) {
		rotate(euler.x, 1, 0, 0);
		rotate(euler.y, 0, 1, 0);
		rotate(euler.z, 0, 0, 1);
	}

	public void scale(Vector3f scale) {
		array[0] = array[0] * scale.x;
		array[1] = array[1] * scale.x;
		array[2] = array[2] * scale.x;
		array[3] = array[3] * scale.x;
		array[4] = array[4] * scale.y;
		array[5] = array[5] * scale.y;
		array[6] = array[6] * scale.y;
		array[7] = array[7] * scale.y;
		array[8] = array[8] * scale.z;
		array[9] = array[9] * scale.z;
		array[10] = array[10] * scale.z;
		array[11] = array[11] * scale.z;
	}
	
	public Vector3f transform(Vector3f v, float w) {
		Vector3f dest = new Vector3f();
		dest.x = array[0] * v.x + array[4] * v.y + array[8] * v.z + array[12] * w;
		dest.y = array[1] * v.x + array[5] * v.y + array[9] * v.z + array[13] * w;
		dest.z = array[2] * v.x + array[6] * v.y + array[10] * v.z + array[14] * w;
		return dest;
	}
	
	public float determinant() {
		float f =
			array[0]
				* ((array[5] * array[10] * array[15] + array[6] * array[11] * array[13] + array[7] * array[9] * array[14])
					- array[7] * array[10] * array[13]
					- array[5] * array[11] * array[14]
					- array[6] * array[9] * array[15]);
		f -= array[1]
			* ((array[4] * array[10] * array[15] + array[6] * array[11] * array[12] + array[7] * array[8] * array[14])
				- array[7] * array[10] * array[12]
				- array[4] * array[11] * array[14]
				- array[6] * array[8] * array[15]);
		f += array[2]
			* ((array[4] * array[9] * array[15] + array[5] * array[11] * array[12] + array[7] * array[8] * array[13])
				- array[7] * array[9] * array[12]
				- array[4] * array[11] * array[13]
				- array[5] * array[8] * array[15]);
		f -= array[3]
			* ((array[4] * array[9] * array[14] + array[5] * array[10] * array[12] + array[6] * array[8] * array[13])
				- array[6] * array[9] * array[12]
				- array[4] * array[10] * array[13]
				- array[5] * array[8] * array[14]);
		return f;
	}
	
	private static float determinant3x3(float t00, float t01, float t02,
		     float t10, float t11, float t12,
		     float t20, float t21, float t22)
	{
	return   t00 * (t11 * t22 - t12 * t21)
	      + t01 * (t12 * t20 - t10 * t22)
	      + t02 * (t10 * t21 - t11 * t20);
	}
	
	public void invert() {
		float determinant = determinant();

		if (determinant != 0) {
			float determinant_inv = 1f/determinant;

			// first row
			float t00 =  determinant3x3(array[5], array[6], array[7], array[9], array[10], array[11], array[13], array[14], array[15]);
			float t01 = -determinant3x3(array[4], array[6], array[7], array[8], array[10], array[11], array[12], array[14], array[15]);
			float t02 =  determinant3x3(array[4], array[5], array[7], array[8], array[9], array[11], array[12], array[13], array[15]);
			float t03 = -determinant3x3(array[4], array[5], array[6], array[8], array[9], array[10], array[12], array[13], array[14]);
			// second row
			float t10 = -determinant3x3(array[1], array[2], array[3], array[9], array[10], array[11], array[13], array[14], array[15]);
			float t11 =  determinant3x3(array[0], array[2], array[3], array[8], array[10], array[11], array[12], array[14], array[15]);
			float t12 = -determinant3x3(array[0], array[1], array[3], array[8], array[9], array[11], array[12], array[13], array[15]);
			float t13 =  determinant3x3(array[0], array[1], array[2], array[8], array[9], array[10], array[12], array[13], array[14]);
			// third row
			float t20 =  determinant3x3(array[1], array[2], array[3], array[5], array[6], array[7], array[13], array[14], array[15]);
			float t21 = -determinant3x3(array[0], array[2], array[3], array[4], array[6], array[7], array[12], array[14], array[15]);
			float t22 =  determinant3x3(array[0], array[1], array[3], array[4], array[5], array[7], array[12], array[13], array[15]);
			float t23 = -determinant3x3(array[0], array[1], array[2], array[4], array[5], array[6], array[12], array[13], array[14]);
			// fourth row
			float t30 = -determinant3x3(array[1], array[2], array[3], array[5], array[6], array[7], array[9], array[10], array[11]);
			float t31 =  determinant3x3(array[0], array[2], array[3], array[4], array[6], array[7], array[8], array[10], array[11]);
			float t32 = -determinant3x3(array[0], array[1], array[3], array[4], array[5], array[7], array[8], array[9], array[11]);
			float t33 =  determinant3x3(array[0], array[1], array[2], array[4], array[5], array[6], array[8], array[9], array[10]);

			// transpose and divide by the determinant
			array[0] = t00*determinant_inv;
			array[5] = t11*determinant_inv;
			array[10] = t22*determinant_inv;
			array[15] = t33*determinant_inv;
			array[1] = t10*determinant_inv;
			array[4] = t01*determinant_inv;
			array[8] = t02*determinant_inv;
			array[2] = t20*determinant_inv;
			array[6] = t21*determinant_inv;
			array[9] = t12*determinant_inv;
			array[3] = t30*determinant_inv;
			array[12] = t03*determinant_inv;
			array[7] = t31*determinant_inv;
			array[13] = t13*determinant_inv;
			array[14] = t23*determinant_inv;
			array[11] = t32*determinant_inv;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(array[0]+" "+array[4]+" "+array[8]+" "+array[12]+"\n");
		sb.append(array[1]+" "+array[5]+" "+array[9]+" "+array[13]+"\n");
		sb.append(array[2]+" "+array[6]+" "+array[10]+" "+array[14]+"\n");
		sb.append(array[3]+" "+array[7]+" "+array[11]+" "+array[15]);
		return sb.toString();
	}
}
