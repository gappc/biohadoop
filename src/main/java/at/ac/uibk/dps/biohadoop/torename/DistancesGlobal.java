package at.ac.uibk.dps.biohadoop.torename;

public class DistancesGlobal {

	private static double[][] distances;
	
	public static void setDistances(double[][] distances) {
		DistancesGlobal.distances = distances;
	}
	
	public static double[][] getDistances() {
		return distances;
	}
}
