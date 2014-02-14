package tests;

import java.util.regex.Pattern;

public class RegTest {
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("\\d+/\\d+/\\d+");
		System.out.println(pattern.matcher("12sd3/123/123").matches());
	}
}
