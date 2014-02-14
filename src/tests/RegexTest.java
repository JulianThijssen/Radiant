package tests;

import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegexTest {
	Pattern vpattern = Pattern.compile("\\d+");
	Pattern vtpattern = Pattern.compile("\\d+/\\d+");
	Pattern vtnpattern = Pattern.compile("\\d+/\\d+/\\d+");
	
	String[] validStrings = {"1/1/1", "123/123/123", "1/1", "123/123", "1", "123"};
	
	@Test
	public void validStrings() {
		assertFalse(matchesV("1/1/1"));
		assertFalse(matchesVT("1/1/1"));
		assertTrue(matchesVTN("1/1/1"));
		
		assertFalse(matchesV("123/123/123"));
		assertFalse(matchesVT("123/123/123"));
		assertTrue(matchesVTN("123/123/123"));
		
		assertFalse(matchesV("1/1"));
		assertTrue(matchesVT("1/1"));
		assertFalse(matchesVTN("1/1"));
		
		assertFalse(matchesV("123/123"));
		assertTrue(matchesVT("123/123"));
		assertFalse(matchesVTN("123/123"));
		
		assertTrue(matchesV("1"));
		assertFalse(matchesVT("1"));
		assertFalse(matchesVTN("1"));
		
		assertTrue(matchesV("123"));
		assertFalse(matchesVT("123"));
		assertFalse(matchesVTN("123"));
	}
	
	@Test
	public void invalidStrings() {
		assertFalse(matchesV("1s/1/1"));
		assertFalse(matchesVT("1s/1/1"));
		assertFalse(matchesVTN("1s/1/1"));
		
		assertFalse(matchesV("123s/123/123"));
		assertFalse(matchesVT("123s/123/123"));
		assertFalse(matchesVTN("123s/123/123"));
		
		assertFalse(matchesV("1s/1"));
		assertFalse(matchesVT("1s/1"));
		assertFalse(matchesVTN("1s/1"));
		
		assertFalse(matchesV("123s/123"));
		assertFalse(matchesVT("123s/123"));
		assertFalse(matchesVTN("123s/123"));
		
		assertFalse(matchesV("1s"));
		assertFalse(matchesVT("1s"));
		assertFalse(matchesVTN("1s"));
		
		assertFalse(matchesV("123s"));
		assertFalse(matchesVT("123s"));
		assertFalse(matchesVTN("123s"));
	}
	
	public boolean matchesV(String s) {
		return vpattern.matcher(s).matches();
	}
	
	public boolean matchesVT(String s) {
		return vtpattern.matcher(s).matches();
	}
	
	public boolean matchesVTN(String s) {
		return vtnpattern.matcher(s).matches();
	}
}
