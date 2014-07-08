package at.ac.uibk.dps.biohadoop.solver.moead.config;

import at.ac.uibk.dps.biohadoop.config.AlgorithmConfiguration;

public class MoeadAlgorithmConfig implements AlgorithmConfiguration {

	private String outputFile;
	private int populationSize;
	private int neighborSize;
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

	public int getNeighborSize() {
		return neighborSize;
	}

	public void setNeighborSize(int neighborSize) {
		this.neighborSize = neighborSize;
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

//	@Override
//	public Object buildParameters() throws BuildParameterException {
//		return new MoeadParameter(maxIterations, populationSize, neighborSize,
//				genomeSize);
//	}

}
