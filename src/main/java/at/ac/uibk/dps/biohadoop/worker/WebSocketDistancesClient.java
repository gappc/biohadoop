package at.ac.uibk.dps.biohadoop.worker;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import at.ac.uibk.dps.biohadoop.websocket.WebSocketDistancesDecoder;

@ClientEndpoint(decoders=WebSocketDistancesDecoder.class)
public class WebSocketDistancesClient {

	private CountDownLatch latch = new CountDownLatch(1);
	
	public WebSocketDistancesClient() {}
			
	public WebSocketDistancesClient(URI uri) throws DeploymentException, IOException,
			InterruptedException {
		
		WebSocketContainer container = ContainerProvider
				.getWebSocketContainer();
		Session session = container.connectToServer(this, uri);
//		session.getBasicRemote().sendText("sdfsf");
		latch.await();
	}
	
	@OnOpen
	public void onOpen(Session session) throws IOException {
		System.out.println("OPENED " + session);
		session.getBasicRemote().sendText("Message from client [ONOPEN]");
	}
	
	@OnClose
    public void onClose(Session session, CloseReason reason) throws IOException {
       System.out.println("CLOSING");
    }
	
	@OnMessage
	public void onMessage(double[][] message) throws DeploymentException, IOException, InterruptedException {
		System.out.println("Received Message: " + message);
		System.out.println("RUNNING CLIENT");
		WebSocketGaClient client = new WebSocketGaClient(URI.create("ws://kleintroppl:30000/websocket/ga"), message);
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
		t.printStackTrace();
	}
}
