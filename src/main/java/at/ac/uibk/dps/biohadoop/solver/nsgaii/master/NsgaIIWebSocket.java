package at.ac.uibk.dps.biohadoop.solver.nsgaii.master;

import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEndpoint;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm.NsgaII;

@ServerEndpoint(value = "/nsgaii", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class NsgaIIWebSocket extends WebSocketEndpoint {

	@Override
	public String getQueueName() {
		return NsgaII.NSGAII_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}

}
