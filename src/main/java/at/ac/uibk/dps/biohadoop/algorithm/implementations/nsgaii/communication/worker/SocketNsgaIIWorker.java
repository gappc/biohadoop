package at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.communication.worker;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.communication.master.NsgaIISocket;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.worker.SocketWorker;

public class SocketNsgaIIWorker extends SocketWorker<double[], double[]> {

	@Override
	public Class<? extends MasterEndpoint> getMasterEndpoint() {
		return NsgaIISocket.class;
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
}
