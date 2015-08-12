package radiant.engine.components;

import radiant.engine.CubeMap;
import radiant.engine.MapType;

public class ReflectionProbe extends Component {
	public CubeMap cubeMap = new CubeMap(MapType.REFLECTION_MAP);
	
	public int getResolution() {
		return cubeMap.getResolution();
	}
	
	public void setResolution(int resolution) {
		cubeMap.setResolution(resolution);
		cubeMap.reset();
	}
}
