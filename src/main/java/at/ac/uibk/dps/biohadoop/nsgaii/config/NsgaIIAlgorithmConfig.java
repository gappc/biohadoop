package at.ac.uibk.dps.biohadoop.nsgaii.config;

import at.ac.uibk.dps.biohadoop.config.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.config.BuildParameterException;

public class NsgaIIAlgorithmConfig implements AlgorithmConfiguration {

	private String outputFile;
	private String algorithm;
	private int populationSize;
	private int genomeSize;
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

	@Override
	public Object buildParameters() throws BuildParameterException {
		return new NsgaIIParameter(maxIterations, populationSize, genomeSize);
	}

}
