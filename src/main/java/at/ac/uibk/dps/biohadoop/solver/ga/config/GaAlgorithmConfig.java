package at.ac.uibk.dps.biohadoop.solver.ga.config;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmConfiguration;

public class GaAlgorithmConfig implements AlgorithmConfiguration {

	private String dataFile;
	private int populationSize;
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

}
