package at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.communication.worker;

import javax.websocket.ClientEndpoint;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.communication.master.NsgaIIWebSocket;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.communication.worker.WebSocketWorker;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class WebSocketNsgaIIWorker extends WebSocketWorker<double[], double[]> {

	@Override
	public Class<? extends MasterEndpoint> getMasterEndpoint() {
		return NsgaIIWebSocket.class;
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

}
