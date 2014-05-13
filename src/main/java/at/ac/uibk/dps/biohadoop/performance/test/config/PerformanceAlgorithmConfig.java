package at.ac.uibk.dps.biohadoop.performance.test.config;


public class PerformanceAlgorithmConfig {

	/**
	 * The algorithm terminates after this number of iterations, on matter if a
	 * (good enough) result is found
	 */
	private int maxIterations;

	private int clientSleepMillis;
	
	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public int getClientSleepMillis() {
		return clientSleepMillis;
	}

	public void setClientSleepMillis(int clientSleepMillis) {
		this.clientSleepMillis = clientSleepMillis;
	}
	
}
