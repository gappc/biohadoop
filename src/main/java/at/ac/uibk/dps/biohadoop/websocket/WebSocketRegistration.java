package at.ac.uibk.dps.biohadoop.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.torename.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.websocket.registration.Registration;

@ServerEndpoint(value = "/ga/register", encoders = WebSocketEncoder.class)
public class WebSocketRegistration {

	private static final Logger logger = LoggerFactory
			.getLogger(WebSocketRegistration.class);

	@OnOpen
	public void open(Session session) {
		logger.info("WebSocket worker registration at URI {}, sessionId={}", session.getRequestURI(), session.getId());
	}

	@OnClose
	public void onClose(Session session) {
		logger.info("WebSocket worker unregistered at URI {}, sessionId={}", session.getRequestURI(), session.getId());
	}

	@OnMessage
	public Registration onMessage(String message, Session session) {
		logger.debug("WebSocket worker message at URI {}, sessionId={}: {}", session.getRequestURI(), session.getId(), message);
		Registration registration = new Registration(DistancesGlobal.getDistances());
		return registration;
	}

	@OnError
	public void onError(Session session, Throwable t) {
		logger.error("WebSocket worker error at URI {}, sessionId={}", session.getRequestURI(), session.getId());
	}
}
