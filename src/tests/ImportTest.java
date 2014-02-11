package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.radiant.geom.Model;
import com.radiant.util.OBJImport;

public class ImportTest {
	@Test
	public void objImport() {
		try {
			Model testModel1 = OBJImport.load("res/Building.obj");
			assertEquals(116166, testModel1.vertices.length);
		} catch(Exception e) {
			e.printStackTrace();
			fail("Loading file threw an exception");
		}
	}
}
