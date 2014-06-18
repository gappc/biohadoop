package at.ac.uibk.dps.biohadoop.torename;

import org.slf4j.Logger;

public class PerformanceLogger {
	
	private long startTime;
	private int counter;
	private int logSteps;
	
	public PerformanceLogger(long startTime, int counter, int logSteps) {
		this.startTime = startTime;
		this.counter = counter;
		this.logSteps = logSteps;
	}

	public int step(Logger log) {
		counter++;
		if (counter % logSteps == 0) {
			long endTime = System.currentTimeMillis();
			log.info("{}ms for last {} computations", endTime - startTime,
					logSteps);
			counter = 0;
			startTime = System.currentTimeMillis();
		}
		return counter;
	}
}
