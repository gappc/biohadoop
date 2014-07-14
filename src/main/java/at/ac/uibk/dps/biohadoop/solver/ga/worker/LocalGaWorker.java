package at.ac.uibk.dps.biohadoop.solver.ga.worker;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorker;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.solver.ga.master.GaLocal;

public class LocalGaWorker extends LocalWorker<int[], Double> {

	private double[][] distances;

	@Override
	public void readRegistrationObject(Object data) {
		distances = (double[][]) data;
	}

	@Override
	public Double compute(int[] data) {
		return GaFitness.computeFitness(distances, data);
	}

	@Override
	public Class<? extends MasterEndpoint> getMasterEndpoint() {
		return GaLocal.class;
	}

}
