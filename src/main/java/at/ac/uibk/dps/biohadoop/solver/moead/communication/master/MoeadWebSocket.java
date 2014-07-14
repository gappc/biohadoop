package at.ac.uibk.dps.biohadoop.solver.moead.communication.master;

import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEndpoint;
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