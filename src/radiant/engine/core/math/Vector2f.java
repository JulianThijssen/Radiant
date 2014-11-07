package radiant.engine.core.math;

import java.nio.FloatBuffer;

public class Vector2f {
	public float x = 0;
	public float y = 0;
	
	public Vector2f() {
		
	}
	
	public Vector2f(float x, float y) {
		set(x, y);
	}
	
	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(Vector2f v) {
		this.x = v.x;
		this.y = v.y;
	}
	
	public Vector2f add(Vector2f v) {
		this.x += v.x;
		this.y += v.y;
		return this;
	}
	
	public Vector2f sub(Vector2f v) {
		this.x -= v.x;
		this.y -= v.y;
		return this;
	}
	
	public Vector2f scale(float scale) {
		this.x *= scale;
		this.y *= scale;
		return this;
	}
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y);
	}
	
	public Vector2f normalise() {
		float length = length();
		this.x /= length;
		this.y /= length;
		return this;
	}

	public void store(FloatBuffer buf) {
		buf.put(x);
		buf.put(y);
	}
	
	@Override
	public String toString() {
		return "<"+x+", "+y+">";
	}
	
	public static Vector2f add(Vector2f v1, Vector2f v2) {
		return new Vector2f(v1.x + v2.x, v1.y + v2.y);
	}
	
	public static Vector2f sub(Vector2f v1, Vector2f v2) {
		return new Vector2f(v1.x - v2.x, v1.y - v2.y);
	}
	
	public static Vector2f scale(Vector2f v, float scale) {
		return new Vector2f(v.x * scale, v.y * scale);
	}
	
	public static Vector2f negate(Vector2f v) {
		return new Vector2f(-v.x, -v.y);
	}
}
