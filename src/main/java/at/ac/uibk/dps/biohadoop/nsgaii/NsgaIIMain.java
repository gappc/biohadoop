package at.ac.uibk.dps.biohadoop.nsgaii;

import at.ac.uibk.dps.biohadoop.nsgaii.algorithm.NsgaII;

public class NsgaIIMain {
	
	private static final int ITERATIONS = 250;
	private static final int POPULATION_SIZE = 500;
	private static final int GENOME_SIZE = 3;
	
	public static void main(String[] args) {
		NsgaII nsgaII = new NsgaII();
		nsgaII.run(ITERATIONS, POPULATION_SIZE, GENOME_SIZE);
	}
}
