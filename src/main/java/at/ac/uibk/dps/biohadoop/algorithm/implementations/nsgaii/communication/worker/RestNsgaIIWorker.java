package at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.communication.worker;

import com.fasterxml.jackson.core.type.TypeReference;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.communication.master.NsgaIIRest;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.worker.RestWorker;

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
