package at.ac.uibk.dps.biohadoop.websocket;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import at.ac.uibk.dps.biohadoop.ga.GaResult;
import at.ac.uibk.dps.biohadoop.ga.GaTask;
import at.ac.uibk.dps.biohadoop.queue.ResultQueue;
import at.ac.uibk.dps.biohadoop.queue.SimpleQueue;

@ServerEndpoint(value = "/ga", encoders = WebSocketEncoder.class, decoders = WebSocketGaResultDecoder.class)
public class GaWebSocketResource {

	@OnOpen
	public void open(Session session) {
		System.out.println("onOpen");
	}

	@OnClose
	public void onClose(Session session) {
		System.out.println("onClose");
	}

	@OnMessage
	public GaTask onMessage(GaResult result, Session session)
			throws InterruptedException {
//		System.out.println("onMessage:" + result);
		ResultQueue.setResult(result.getSlot(), result.getResult());
		return (GaTask) SimpleQueue.take();
	}

	@OnError
	public void onError(Session session, Throwable t) {
		System.out.println("onerror");
		t.printStackTrace();
	}
}
