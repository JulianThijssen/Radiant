package com.radiant;

import com.radiant.entities.Entity;

public interface Script {
	public void onStart();
	
	public void update(Entity e);
	
	public void onStop();
}
