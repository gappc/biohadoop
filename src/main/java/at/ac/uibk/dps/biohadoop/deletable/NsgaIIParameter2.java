package at.ac.uibk.dps.biohadoop.deletable;

public class NsgaIIParameter2 {

	private final int maxIterations;
	private final int populationSize;
	private final int genomeSize;

	public NsgaIIParameter2(int maxIterations, int populationSize, int genomeSize) {
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
