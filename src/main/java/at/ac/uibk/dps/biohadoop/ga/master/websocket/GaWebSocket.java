package at.ac.uibk.dps.biohadoop.ga.master.websocket;

import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketEndpoint;
import at.ac.uibk.dps.biohadoop.ga.master.GaMasterImpl;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;

@ServerEndpoint(value = "/ga", encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class GaWebSocket extends WebSocketEndpoint {
	
	public GaWebSocket() {
		masterEndpoint = new GaMasterImpl(this);
	}
	
}
