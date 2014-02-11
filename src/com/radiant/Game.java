package com.radiant;

import obj.radiant.exceptions.RadiantException;

public interface Game {	
	public void start() throws RadiantException;
	
	public void update();
	
	public void render();
	
	public void stop();
}
