package radiant.assets.model;

public class Face {
	public int[] vi;
	public int[] ti;
	public int[] ni;

	public Face() {
		
	}
	
	public Face(int[] vertices) {
		this(vertices, null, null);
	}
	
	public Face(int[] vi, int[] ti, int[] ni) {
		this.vi = vi;
		this.ti = ti;
		this.ni = ni;
	}
}
