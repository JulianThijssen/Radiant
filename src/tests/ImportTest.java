package tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lwjgl.opengl.Display;

import com.radiant.util.OBJLoader;

public class ImportTest {
	@Test
	public void standard() {
		try {
			Display.create();
			OBJLoader.loadMesh("res/unit_OBJ_standard.obj");
			Display.destroy();
			assertTrue("OBJ loaded without exceptions", true);
		} catch(Exception e) {
			Display.destroy();
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void full() {
		try {
			Display.create();
			OBJLoader.loadMesh("res/Lightning/lightning.obj");
			Display.destroy();
			assertTrue("OBJ loaded without exceptions", true);
		} catch(Exception e) {
			Display.destroy();
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	@Test
	public void badVertex() {
		try {
			Display.create();
			OBJLoader.loadMesh("res/unit_OBJ_bad_vertex.obj");
			Display.destroy();
			fail("OBJ Loader didn't throw an exception for a wrong vertex");
		} catch(Exception e) {
			Display.destroy();
			System.out.println(e.getMessage());
			assertTrue("Loading file threw an exception for a wrong vertex", true);
		}
	}
	
	@Test
	public void badNormal() {
		try {
			Display.create();
			OBJLoader.loadMesh("res/unit_OBJ_bad_normal.obj");
			Display.destroy();
			fail("OBJ Loader didn't throw an exception for a wrong normal");
		} catch(Exception e) {
			Display.destroy();
			System.out.println(e.getMessage());
			assertTrue("Loading file threw an exception for a wrong normal", true);
		}
	}
	
	@Test
	public void badFace() {
		try {
			Display.create();
			OBJLoader.loadMesh("res/unit_OBJ_bad_face.obj");
			Display.destroy();
			fail("OBJ Loader didn't throw an exception for a wrong face");
		} catch(Exception e) {
			Display.destroy();
			System.out.println(e.getMessage());
			assertTrue("Loading file threw an exception for a wrong face", true);
		}
	}
}
