package at.ac.uibk.dps.biohadoop.solver.ga;

public class DistancesGlobal {

	private static double[][] distances;
	private static Double[][] distancesAsObject;

	private DistancesGlobal() {
	}

	public static void setDistances(double[][] distances) {
		DistancesGlobal.distances = distances;
	}

	public static double[][] getDistances() {
		return distances;
	}

	public static Double[][] getDistancesAsObject() {
		if (distancesAsObject == null) {
			int length1 = distances.length;
			int length2 = length1 != 0 ? distances[0].length : 0; 
			distancesAsObject = new Double[length1][length2];
			for (int i = 0; i < length1; i++) {
				for (int j = 0; j < length2; j++) {
					distancesAsObject[i][j] = distances[i][j];
				}
			}
		}
		return distancesAsObject;
	}

}
