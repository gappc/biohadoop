package at.ac.uibk.dps.biohadoop.solver.nsgaii.config;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmConfiguration;

public class NsgaIIAlgorithmConfig implements AlgorithmConfiguration {

	private String outputFile;
	private int populationSize;
	private int genomeSize;
	private int maxIterations;

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
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
