package at.ac.uibk.dps.biohadoop.solver.ga.algorithm;

public class Tsp {

	double[][] cities = new double[0][0];
	double[][] distances = new double[0][0];
	
	public double[][] getCities() {
		return cities;
	}
	public void setCities(double[][] cities) {
		this.cities = cities;
	}
	public double[][] getDistances() {
		return distances;
	}
	public void setDistances(double[][] distances) {
		this.distances = distances;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Cities: ");
		for (double[] city : cities) {
			sb.append("[");
			sb.append(city[0]);
			sb.append("]");
			sb.append("[");
			sb.append(city[1]);
			sb.append("] ");
		}
		
		sb.append("\nDistances: ");
		for (double[] distance : distances) {
			for (double d : distance) {
				sb.append(d);
				sb.append(" ");
			}
			sb.append("\n");
		}
		
		sb.append("\n");
		
		return sb.toString();
	}
}
