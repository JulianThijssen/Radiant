package radiant.assets.model;

public class Face {
	public static final int VERTICES_PER_FACE = 3;
	
	public int[] vi;
	public int[] ti;
	public int[] ni;

	public Face() {
		this(new int[VERTICES_PER_FACE], new int[VERTICES_PER_FACE], new int[VERTICES_PER_FACE]);
	}
	
	public Face(int[] vi) {
		this(vi, null, null);
	}
	
	public Face(int[] vi, int[] ti, int[] ni) {
		this.vi = vi;
		this.ti = ti;
		this.ni = ni;
	}
}
