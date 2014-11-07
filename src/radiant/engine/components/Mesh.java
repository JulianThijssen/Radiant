package radiant.engine.components;

import java.util.ArrayList;

import radiant.assets.model.Face;
import radiant.engine.core.math.Vector2f;
import radiant.engine.core.math.Vector3f;

public class Mesh extends Component {
	public String name;
	public ArrayList<Vector3f> vertices = null;
	public ArrayList<Vector2f> texCoords = null;
	public ArrayList<Vector3f> normals = null;
	public ArrayList<Vector3f> tangents = null;
	public ArrayList<Face> faces = new ArrayList<Face>();
	
	public int materialIndex = -1;
	
	public int handle;
	
	public Mesh(String name) {
		this.name = name;
	}
	
	public int getNumFaces() {
		return faces.size();
	}
}
