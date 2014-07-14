package at.ac.uibk.dps.biohadoop.algorithm.implementations.moead.communication.worker;

import javax.websocket.ClientEndpoint;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.moead.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.moead.communication.master.MoeadWebSocket;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.communication.worker.WebSocketWorker;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class WebSocketMoeadWorker extends WebSocketWorker<double[], double[]> {

	@Override
	public Class<? extends MasterEndpoint> getMasterEndpoint() {
		return MoeadWebSocket.class;
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

	@Override
	public String getPath() {
		return "/moead";
	}

}
