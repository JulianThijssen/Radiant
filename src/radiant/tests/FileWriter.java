package radiant.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FileWriter {
	public static void writeToFile(float[] array, int width, int height) {
		PrintWriter out;
		try {
			out = new PrintWriter(new File("output.txt"));
		
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					out.print(array[y * width + x] + ",");
				}
				out.println();
			}
			
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
