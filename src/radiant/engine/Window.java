package radiant.engine;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import radiant.engine.core.diag.Log;
import radiant.engine.core.errors.RadiantException;

public final class Window {
	private static final String DEFAULT_TITLE = "Radiant Game";
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	
	public static String title = DEFAULT_TITLE;
	public static int width = DEFAULT_WIDTH;
	public static int height = DEFAULT_HEIGHT;
	
	private DisplayMode currentDisplayMode;
	
	public Window() {
		
	}
	
	public void create() throws RadiantException {
		create(title, width, height);
	}
	
	public void create(String title) throws RadiantException {
		create(title, width, height);
	}
	
	public void create(String title, int width, int height) throws RadiantException {
		try {
			setTitle(title);
			setSize(width, height);
			PixelFormat pixelFormat = new PixelFormat().withDepthBits(24).withStencilBits(8);
			ContextAttribs contextAtrributes = new ContextAttribs(3, 2).withForwardCompatible(true).withProfileCore(true);
			Display.create(pixelFormat, contextAtrributes);
		} catch(LWJGLException e) {
			throw new RadiantException("Window context can not be created");
		}
	}
	
	public String getTitle() {
		return Display.getTitle();
	}
	
	public void setTitle(String title) {
		Window.title = title;
		Display.setTitle(title);
	}
	
	public void setSize(int width, int height) {
		Window.width = width;
		Window.height = height;
		currentDisplayMode = new DisplayMode(width, height);
		try {
			Display.setDisplayMode(currentDisplayMode);
		} catch (LWJGLException e) {
			Log.debug("Failed to resize the window");
		}
	}
	
	public void update() {
		Display.update();
	}
	
	public boolean isClosed() {
		return Display.isCloseRequested();
	}
	
	public void destroy() {
		Display.destroy();
	}
}
