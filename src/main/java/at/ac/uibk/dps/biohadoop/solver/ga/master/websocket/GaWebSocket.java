package at.ac.uibk.dps.biohadoop.solver.ga.master.websocket;

import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketEndpoint;
import at.ac.uibk.dps.biohadoop.solver.ga.master.GaMasterImpl;

@ServerEndpoint(value = "/ga", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class GaWebSocket extends WebSocketEndpoint {
	
	public GaWebSocket() {
		masterEndpoint = new GaMasterImpl(this);
	}
	
}