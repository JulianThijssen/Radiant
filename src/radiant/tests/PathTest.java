package radiant.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import radiant.engine.core.file.Path;

public class PathTest {	
	@Test
	public void extensionTest() {
		Path path = new Path("/res/category/file.ext");
		assertEquals(path.getExtension(), ".ext");
	}
	
	@Test
	public void folderTest() {
		Path path = new Path("/res/category/file.ext");
		assertEquals(path.getCurrentFolder().toString(), "/res/category/");
	}
}
