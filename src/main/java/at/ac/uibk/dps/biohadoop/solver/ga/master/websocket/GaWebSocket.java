package at.ac.uibk.dps.biohadoop.solver.ga.master.websocket;

import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketEndpoint;
import at.ac.uibk.dps.biohadoop.solver.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;

@ServerEndpoint(value = "/ga", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class GaWebSocket extends WebSocketEndpoint {

	@Override
	public String getQueueName() {
		return Ga.GA_QUEUE;
	}

	@Override
	public String getPrefix() {
		return "GA";
	}

	@Override
	public Object getRegistrationObject() {
		return DistancesGlobal.getDistancesAsObject();
	}

}
