package radiant.engine.core.file;

public class Path {
	private String path = null;
	
	public Path(String path) {
		this.path = path;
		correctSlashes();
	}
	
	public Path concat(Path path) {
		this.path = this.path.concat(path.path);
		return this;
	}
	
	public static Path concat(Path head, Path tail) {
		return new Path(head.path.concat(tail.path));
	}
	
	public Path getCurrentFolder() {
		int index = path.lastIndexOf('/') + 1;
		if(index != -1) {
			Path folder = new Path(path.substring(0, index));
			return folder;
		}
		return null;
	}
	
	public String getExtension() {
		int index = path.lastIndexOf('.');
		if(index != -1) {
			String extension = path.substring(index);
			return extension;
		}
		return "";
	}
	
	private void correctSlashes() {
		path = path.replace('\\', '/');
	}
	
	@Override
	public String toString() {
		return path;
	}
	
	@Override
	public boolean equals(Object object) {
		Path path = (Path) object;
		if(path.toString().equals(this.path)) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hashCode = 0;
		
		for(int i = 0; i < path.length(); i++) {
			hashCode += path.charAt(i);
		}
		
		return hashCode;
	}
}
