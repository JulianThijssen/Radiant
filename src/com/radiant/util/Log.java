package com.radiant.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Log {
	
	public static void info(String info) {
		System.out.println(info);
	}
	
	public static void debug(String debug) {
		System.out.println(debug);
	}
	
	public static void error(String error) {
		System.out.println(error);
	}
	
	public static void log(String log) {
		File file = new File("log.txt");
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(file, true));
			out.println(log);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			out.close();
		}
	}
}
