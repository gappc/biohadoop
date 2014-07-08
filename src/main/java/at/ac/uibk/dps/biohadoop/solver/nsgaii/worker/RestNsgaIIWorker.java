package at.ac.uibk.dps.biohadoop.solver.nsgaii.worker;

import at.ac.uibk.dps.biohadoop.connectionworker.RestWorker;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.master.NsgaIIRest;

public class RestNsgaIIWorker extends RestWorker<double[], double[]> {

	@Override
	public Class<? extends Master> getMasterEndpoint() {
		return NsgaIIRest.class;
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

	@Override
	public String getPath() {
		return "/nsgaii";
	}

}
