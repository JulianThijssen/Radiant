package com.radiant.util;

import java.nio.FloatBuffer;

public class Vector3f {
	public float x = 0;
	public float y = 0;
	public float z = 0;
	
	public Vector3f() {
		
	}
	
	public Vector3f(float x, float y, float z) {
		set(x, y, z);
	}
	
	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void set(Vector3f v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	public Vector3f add(Vector3f v) {
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
		return this;
	}
	
	public Vector3f sub(Vector3f v) {
		this.x -= v.x;
		this.y -= v.y;
		this.z -= v.z;
		return this;
	}
	
	public Vector3f scale(float scale) {
		this.x *= scale;
		this.y *= scale;
		this.z *= scale;
		return this;
	}
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	public Vector3f normalise() {
		float length = length();
		this.x /= length;
		this.y /= length;
		this.z /= length;
		return this;
	}
	
	public void load(FloatBuffer buf) {
		x = buf.get();
		y = buf.get();
		z = buf.get();
	}
	
	public void store(FloatBuffer buf) {
		buf.put(x);
		buf.put(y);
		buf.put(z);
	}

	@Override
	public String toString() {
		return "<"+x+", "+y+", "+z+">";
	}
	
	public static Vector3f add(Vector3f v1, Vector3f v2) {
		return new Vector3f(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
	}
	
	public static Vector3f sub(Vector3f v1, Vector3f v2) {
		return new Vector3f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
	}
	
	public static Vector3f scale(Vector3f v, float scale) {
		return new Vector3f(v.x * scale, v.y * scale, v.z * scale);
	}
	
	public static float dot(Vector3f v1, Vector3f v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}
	
	public static Vector3f negate(Vector3f v) {
		return new Vector3f(-v.x, -v.y, -v.z);
	}
}
