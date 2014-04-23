package com.radiant;

public interface Script {
	public void onStart();
	
	public void update(Scene scene);
	
	public void onStop();
}
