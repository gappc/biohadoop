package at.ac.uibk.dps.biohadoop.ga.algorithm;

public class GaFitness {

	public static double computeFitness(double[][] distances, int[] ds) {
		double pathLength = 0.0;
		for (int i = 0; i < ds.length - 1; i++) {
			pathLength += distances[ds[i]][ds[i + 1]];
		}

		pathLength += distances[ds[ds.length - 1]][ds[0]];

		return pathLength;
	}

}
