package radiant.tests;

import static org.junit.Assert.*;

import java.util.HashMap;

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
	
	@Test
	public void equalsTest() {
		Path path1 = new Path("/res/category/file.ext");
		Path path2 = new Path("/res/category/file.ext");
		assertEquals(path1, path2);
	}
	
	@Test
	public void hashMapTest() {
		Path path1 = new Path("/res/category/file.ext");
		Path path2 = new Path("/res/category/file.ext");
		
		HashMap<Path, String> map = new HashMap<Path, String>();
		map.put(path1, "Test");
		
		if(map.containsKey(path2)) {
			assertEquals(1, 1);
		} else {
			fail();
		}
	}
}
