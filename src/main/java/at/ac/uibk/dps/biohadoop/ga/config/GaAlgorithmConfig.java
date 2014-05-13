package at.ac.uibk.dps.biohadoop.ga.config;


public class GaAlgorithmConfig {

	/**
	 * Filename for input data
	 */
	private String dataFile;
	
	/**
	 * Classname for algorithm, that should be run
	 */
	private String algorithm;

	/**
	 * Number of genomes, that are used for computation
	 */
	private int populationSize;

	/**
	 * The algorithm terminates after this number of iterations, on matter if a
	 * (good enough) result is found
	 */
	private int maxIterations;

	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	
}