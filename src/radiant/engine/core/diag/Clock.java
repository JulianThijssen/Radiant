package radiant.engine.core.diag;

public class Clock {
	private long startTime = -1;
	private long endTime = -1;
	
	public Clock() {
		
	}
	
	/**
	 * Sets the current time as the start time of the clock
	 */
	public void start() {
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * Sets the current time as the end time of the clock
	 */
	public void end() {
		endTime = System.currentTimeMillis();
	}
	
	/**
	 * Returns the difference between the end time and start time of the clock
	 * in milliseconds, returns -1 if one or both of the times are not set.
	 * @return
	 */
	public int getMilliseconds() {
		if(startTime == -1 || endTime == -1) {
			return -1;
		}
		return (int) (endTime - startTime);
	}
	
	/**
	 * Returns the difference between the end time and start time of the clock
	 * in seconds, returns -1 if one or both of the times are not set.
	 * @return
	 */
	public float getSeconds() {
		if(startTime == -1 || endTime == -1) {
			return -1;
		}
		return (float) (endTime - startTime) / 1000;
	}
}
