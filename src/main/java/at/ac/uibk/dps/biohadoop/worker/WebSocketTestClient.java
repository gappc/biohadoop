package at.ac.uibk.dps.biohadoop.worker;

import java.net.URI;

//@ClientEndpoint(decoders=InjectionBeanDecoder.class)
public class WebSocketTestClient {

	// private URI uri;
	// private CountDownLatch latch = new CountDownLatch(1);

	public static void main(String[] args) throws Exception {
		// WebSocketTestClient testClient = new WebSocketTestClient();
		// testClient.uri = URI.create("ws://kleintroppl:30000/websocket/ga");
		// testClient.run();
		WebSocketDistancesClient distancesClient = new WebSocketDistancesClient(
				URI.create("ws://kleintroppl:30000/websocket/ga/register"));
	}

	// public WebSocketTestClient(){
	//
	// }
	//
	// public void run() {
	// System.out.println("sdfasdf");
	// try {
	// WebSocketContainer container = ContainerProvider
	// .getWebSocketContainer();
	// Session session = container.connectToServer(this, uri);
	// session.getBasicRemote().sendText("sdfsf");
	// latch.await();
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// @OnOpen
	// public void onOpen(Session userSession) {
	// System.out.println("OPENED " + userSession);
	// }
	//
	// @OnClose
	// public void onClose(Session session, CloseReason reason) throws
	// IOException {
	// System.out.println("CLOSING");
	// }
	//
	// @OnMessage
	// public void onMessage(InjectionBean message) {
	// System.out.println("Received Message: " + message);
	// }
	//
	// @OnError
	// public void onError(Session session, Throwable t) {
	// t.printStackTrace();
	// }

}
