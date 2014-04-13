package at.ac.uibk.dps.biohadoop.ga;

public class DistancesGlobal {

	private static double[][] distances;
	
	private DistancesGlobal() {
	}
	
	public static void setDistances(double[][] distances) {
		DistancesGlobal.distances = distances;
	}
	
	public static double[][] getDistances() {
		return distances;
	}
}
