package radiant.engine.core.diag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Log {
	private static PrintStream out = System.out;
	
	public static void info(String info) {
		out.println(info);
	}
	
	public static void debug(String debug) {
		out.println(debug);
	}
	
	public static void error(String error) {
		out.println(error);
		System.exit(1);
	}
	
	public static void setOutput(PrintStream output) {
		out = output;
	}
	
	public static void setOutput(File file) throws FileNotFoundException {
		out = new PrintStream(file);
	}
}
