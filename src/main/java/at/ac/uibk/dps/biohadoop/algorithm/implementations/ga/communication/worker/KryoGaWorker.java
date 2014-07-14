package at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.worker;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.master.GaKryo;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.worker.KryoWorker;

public class KryoGaWorker extends KryoWorker<int[], Double> {

	private double[][] distances;
	
	@Override
	public Class<? extends MasterEndpoint> getMasterEndpoint() {
		return GaKryo.class;
	}
	
	@Override
	public void readRegistrationObject(Object data) {
		Double[][] inputDistances = (Double[][])data;
		int length1 = inputDistances.length;
		int length2 = length1 != 0 ? inputDistances[0].length : 0;
		distances = new double[length1][length2];
		for (int i = 0; i < length1; i++) {
			for (int j = i; j < length2; j++) {
				distances[i][j] = inputDistances[i][j];
				distances[j][i] = inputDistances[j][i];
			}
		}
	}

	@Override
	public Double compute(int[] data) {
		return GaFitness.computeFitness(distances, data);
	}

}
