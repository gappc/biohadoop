package at.ac.uibk.dps.biohadoop.nsgaii.config;


public class NsgaIIAlgorithmConfig {

	/**
	 * Filename for result
	 */
	private String outputFile;
	
	/**
	 * Classname for algorithm, that should be run
	 */
	private String algorithm;

	private int populationSize;
	
	private int genomeSize;

	/**
	 * The algorithm terminates after this number of iterations, on matter if a
	 * (good enough) result is found
	 */
	private int maxIterations;

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public int getGenomeSize() {
		return genomeSize;
	}

	public void setGenomeSize(int genomeSize) {
		this.genomeSize = genomeSize;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

}
