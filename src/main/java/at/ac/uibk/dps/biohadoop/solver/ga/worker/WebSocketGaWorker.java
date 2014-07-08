package at.ac.uibk.dps.biohadoop.solver.ga.worker;

import javax.websocket.ClientEndpoint;

import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.connectionworker.WebSocketWorker;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.solver.ga.master.websocket.GaWebSocket;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class WebSocketGaWorker extends WebSocketWorker<int[], Double> {

	private double[][] distances;

	@Override
	public Class<? extends Master> getMasterEndpoint() {
		return GaWebSocket.class;
	}
	
	@Override
	public void readRegistrationObject(Object data) {
		double[][] inputDistances = (double[][]) data;
		int length1 = inputDistances.length;
		int length2 = length1 != 0 ? inputDistances[0].length : 0;
		distances = new double[length1][length2];
		for (int i = 0; i < length1; i++) {
			for (int j = i; j < length2; j++) {
				distances[i][j] = inputDistances[i][j];
				distances[j][i] = inputDistances[j][i];
			}
		}
	}

	@Override
	public Double compute(int[] data) {
		return GaFitness.computeFitness(distances, data);
	}

	@Override
	public String getPath() {
		return "/ga";
	}
}
