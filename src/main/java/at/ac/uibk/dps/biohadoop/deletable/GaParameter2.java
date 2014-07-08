package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Tsp;

public class GaParameter2 {

	private final Tsp tsp;
	private final int populationSize;
	private final int maxIterations;

	public GaParameter2(Tsp tsp, int populationSize, int maxIterations) {
		this.tsp = tsp;
		this.populationSize = populationSize;
		this.maxIterations = maxIterations;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public Tsp getTsp() {
		return tsp;
	}
}
