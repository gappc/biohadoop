package at.ac.uibk.dps.biohadoop.algorithm.implementations.moead.communication.master;

import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEndpoint;

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
