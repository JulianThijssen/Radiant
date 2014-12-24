package radiant.engine.core.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import radiant.engine.core.diag.Log;

public class FileIO {
	public static String loadAsString(String path) {
		if (path == null) {
			Log.error("Path can not be loaded, because it is null");
			return "";
		}
		
		File file = new File(path);
		BufferedReader in = null;
		
		try {
			in = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			Log.error("Could not find file: " + path);
			return "";
		}
		
		StringBuilder output = new StringBuilder();
		String line = null;
		
		try {
			while ((line = in.readLine()) != null) {
				output.append(line + "\n");
			}
			in.close();
		} catch(IOException e) {
			Log.error("An error occurred while loading file: " + path);
			return "";
		}
		
		return output.toString();
	}
}
