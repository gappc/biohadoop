package at.ac.uibk.dps.biohadoop.solver.ga.algorithm;

import java.util.List;

public class GaFitness {

	private GaFitness() {
	}
	
	public static double computeFitness(double[][] distances, int[] ds) {
		double pathLength = 0.0;
		for (int i = 0; i < ds.length - 1; i++) {
			pathLength += distances[ds[i]][ds[i + 1]];
		}

		pathLength += distances[ds[ds.length - 1]][ds[0]];

		return pathLength;
	}
	
	public static double computeFitness(double[][] distances, List<Integer> ds) {
		double pathLength = 0.0;
		for (int i = 0; i < ds.size() - 1; i++) {
			pathLength += distances[ds.get(i)][ds.get(i + 1)];
		}

		pathLength += distances[ds.get(ds.size() - 1)][ds.get(0)];

		return pathLength;
	}

}
