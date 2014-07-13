package at.ac.uibk.dps.biohadoop.solver.nsgaii.worker;

import javax.websocket.ClientEndpoint;

import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.connectionworker.WebSocketWorker;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.master.NsgaIIWebSocket;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class WebSocketNsgaIIWorker extends WebSocketWorker<double[], double[]> {

	@Override
	public Class<? extends Master> getMasterEndpoint() {
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
