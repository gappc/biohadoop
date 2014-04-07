package at.ac.uibk.dps.biohadoop.queue;

public class ResultQueue {

	private static int count = 0;
//	TODO make dynamic
	private static double[] results = new double[1024];

	public static double[] getResults() {
		return results;
	}

	public static void setResult(int slot, double result) {
		results[slot] = result;
		count++;
	}
	
	public static void reset() {
		count = 0;
	}
	
	public static int getCount() {
		return count;
	}
}
