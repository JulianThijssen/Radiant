package tests;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class RotationTest {
	public static void main(String[] args) {
		Matrix4f m = new Matrix4f();
		Vector3f v = new Vector3f(0.70711f, -0.70711f, 0);
		Vector4f dest = new Vector4f(0, 0, 0, 0);
		Vector3f euler = new Vector3f(-45, -45, 0);
		
		m.rotate((float) Math.toRadians(90), new Vector3f(0, 1, 0));
		m.rotate((float) Math.toRadians(45), new Vector3f(1, 0, 0));
		
		//m.translate(new Vector3f(0, -1, 0));
		
		System.out.printf("%f,%f,%f,%f\n", m.m00, m.m10, m.m20, m.m30);
		System.out.printf("%f,%f,%f,%f\n", m.m01, m.m11, m.m21, m.m31);
		System.out.printf("%f,%f,%f,%f\n", m.m02, m.m12, m.m22, m.m32);
		System.out.printf("%f,%f,%f,%f\n", m.m03, m.m13, m.m23, m.m33);
		
		Vector3f dir = toAxisAngle(euler);
		//System.out.printf("%f,%f,%f,\n", dir.x, dir.y, dir.z);
		dir = rotate(euler.y, euler.x, euler.z);
		//System.out.printf("%f,%f,%f,\n", dir.x, dir.y, dir.z);
		
		Matrix4f.transform(m, new Vector4f(v.x, v.y, v.z, 1), dest);
		System.out.printf("%f,%f,%f,%f\n", dest.x, dest.y, dest.z, dest.w);
	}
	
	public static Vector3f toAxisAngle(Vector3f euler) {
		float c1 = (float) Math.cos(euler.y/2);
		float c2 = (float) Math.cos(euler.x/2);
		float c3 = (float) Math.cos(euler.z/2);
		float s1 = (float) Math.sin(euler.y/2);
		float s2 = (float) Math.sin(euler.x/2);
		float s3 = (float) Math.sin(euler.z/2);
		
		float angle = (float) (2 * Math.acos(c1*c2*c3 - s1*s2*s3));
		Vector3f dir = new Vector3f();
		dir.x = s1*s2*c3 + c1*c2*s3;
		dir.y = s1*c2*c3 + c1*s2*s3;
		dir.z = c1*s2*c3 - s1*c2*s3;
		dir.normalise();
		return dir;
	}

	public final static Vector3f rotate(double heading, double attitude, double bank) {
		// Assuming the angles are in radians.
		double c1 = Math.cos(heading / 2);
		double s1 = Math.sin(heading / 2);
		double c2 = Math.cos(attitude / 2);
		double s2 = Math.sin(attitude / 2);
		double c3 = Math.cos(bank / 2);
		double s3 = Math.sin(bank / 2);
		double c1c2 = c1 * c2;
		double s1s2 = s1 * s2;
		double w = c1c2 * c3 - s1s2 * s3;
		double x = c1c2 * s3 + s1s2 * c3;
		double y = s1 * c2 * c3 + c1 * s2 * s3;
		double z = c1 * s2 * c3 - s1 * c2 * s3;
		double angle = 2 * Math.acos(w);
		double norm = x * x + y * y + z * z;
		if (norm < 0.001) { // when all euler angles are zero angle =0 so
			// we can set axis to anything to avoid divide by zero
			x = 1;
			y = z = 0;
		} else {
			norm = Math.sqrt(norm);
			x /= norm;
			y /= norm;
			z /= norm;
		}
		Vector3f dir = new Vector3f((float) x, (float) y, (float) z);
		return dir;
	}
}
