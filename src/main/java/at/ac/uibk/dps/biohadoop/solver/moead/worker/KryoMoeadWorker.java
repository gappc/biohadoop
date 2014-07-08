package at.ac.uibk.dps.biohadoop.solver.moead.worker;

import at.ac.uibk.dps.biohadoop.connectionworker.KryoWorker;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.solver.moead.master.MoeadKryo;

public class KryoMoeadWorker extends KryoWorker<double[], double[]> {

	@Override
	public Class<? extends Master> getMasterEndpoint() {
		return MoeadKryo.class;
	}
	
	@Override
	public void readRegistrationObject(Object data) {
	}

	@Override
	public double[] compute(double[] data) {
		double[] result = new double[2];
		result[0] = Functions.f1(data);
		result[1] = Functions.f2(data);
		return result;
	}
}
