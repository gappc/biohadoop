package at.ac.uibk.dps.biohadoop.algorithm.implementations.moead.communication.worker;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.moead.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.moead.communication.master.MoeadLocal;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorker;

public class LocalMoeadWorker extends LocalWorker<double[], double[]> {

	@Override
	public Class<? extends MasterEndpoint> getMasterEndpoint() {
		return MoeadLocal.class;
	}

	@Override
	public void readRegistrationObject(Object data) {
		// No registration object for MOEAD
	}

	@Override
	public double[] compute(double[] data) {
		double[] result = new double[2];
		result[0] = Functions.f1(data);
		result[1] = Functions.f2(data);
		return result;
	}

}