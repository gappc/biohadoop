package at.ac.uibk.dps.biohadoop.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.torename.DistancesGlobal;

@ServerEndpoint(value="/ga/register", encoders=WebSocketEncoder.class)
public class WebSocketRegistration {

	@OnOpen
	public void open(Session session) {
		System.out.println("Registration opened");
	}

	@OnClose
	public void onClose(Session session) {
		System.out.println("Registration closed");
	}

	@OnMessage
	public double[][] onMessage(String message, Session session) {
		System.out.println("Registration:" + message);
		return DistancesGlobal.getDistances();
	}

	@OnError
	public void onError(Session session, Throwable t) {
		System.out.println("Registration error");
		t.printStackTrace();
	}
}
