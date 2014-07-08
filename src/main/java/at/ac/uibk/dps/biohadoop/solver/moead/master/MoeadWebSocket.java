package at.ac.uibk.dps.biohadoop.solver.moead.master;

import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketEndpoint;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Moead;

@ServerEndpoint(value = "/moead", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class MoeadWebSocket extends WebSocketEndpoint {

	@Override
	public String getQueueName() {
		return Moead.MOEAD_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}

}
