package at.ac.uibk.dps.biohadoop.solver.nsgaii.worker;

import com.fasterxml.jackson.core.type.TypeReference;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connectionworker.RestWorker;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.master.NsgaIIRest;

public class RestNsgaIIWorker extends RestWorker<double[], double[]> {

	@Override
	public Class<? extends MasterEndpoint> getMasterEndpoint() {
		return NsgaIIRest.class;
	}

	@Override
	public void readRegistrationObject(Object data) {
		// No registration object for NSGA-II
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

	@Override
	public TypeReference<Message<double[]>> getInputType() {
		return new TypeReference<Message<double[]>>() {
		};
	}

}
