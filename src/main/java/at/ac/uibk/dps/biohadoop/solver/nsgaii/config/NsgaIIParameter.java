package at.ac.uibk.dps.biohadoop.solver.nsgaii.config;

public class NsgaIIParameter {

	private final int maxIterations;
	private final int populationSize;
	private final int genomeSize;

	public NsgaIIParameter(int maxIterations, int populationSize, int genomeSize) {
		this.maxIterations = maxIterations;
		this.populationSize = populationSize;
		this.genomeSize = genomeSize;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public int getGenomeSize() {
		return genomeSize;
	}

}
