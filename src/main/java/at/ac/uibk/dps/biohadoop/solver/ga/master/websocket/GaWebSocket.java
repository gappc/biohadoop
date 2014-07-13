package at.ac.uibk.dps.biohadoop.solver.ga.master.websocket;

import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEndpoint;
import at.ac.uibk.dps.biohadoop.solver.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;

@ServerEndpoint(value = "/ga", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class GaWebSocket extends WebSocketEndpoint {

	@Override
	public String getQueueName() {
		return Ga.GA_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return DistancesGlobal.getDistances();
	}

}
