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

import at.ac.uibk.dps.biohadoop.ga.GaResult;
import at.ac.uibk.dps.biohadoop.ga.GaTask;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketGaTaskDecoder;

@ClientEndpoint(encoders=WebSocketEncoder.class, decoders=WebSocketGaTaskDecoder.class)
public class WebSocketGaClient {

	private CountDownLatch latch = new CountDownLatch(1);
	private double[][] distances;
	private long start;
	
	public WebSocketGaClient() {}
	
	public WebSocketGaClient(URI uri, double[][] distances) throws DeploymentException, IOException,
			InterruptedException {
		this.start = System.currentTimeMillis();
		this.distances = distances;
		WebSocketContainer container = ContainerProvider
				.getWebSocketContainer();
		Session session = container.connectToServer(this, uri);
//		session.getBasicRemote().sendText("sdfsf");
		latch.await();
	}
	
	@OnOpen
	public void onOpen(Session session) throws IOException {
		System.out.println("WebSocketGaClient OPENED " + session);
		session.getBasicRemote().sendText("{\"slot\": 0, \"result\": 200000.234234}");
	}
	
	@OnClose
    public void onClose(Session session, CloseReason reason) throws IOException {
       System.out.println("WebSocketGaClient CLOSING");
    }
	
	@OnMessage
	public GaResult onMessage(GaTask task) {
		System.out.println("WebSocketGaClient Received Message: " + (System.currentTimeMillis() - start)  + "ms");
		this.start = System.currentTimeMillis();
		GaResult gaResult = new GaResult();
		gaResult.setSlot(task.getSlot());
		gaResult.setResult(fitness(distances, task.getGenome()));
		return gaResult;
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
		t.printStackTrace();
	}
	
	private static double fitness(double[][] distances, int[] ds) {
		double pathLength = 0.0;
		for (int i = 0; i < ds.length - 1; i++) {
			pathLength += distances[ds[i]][ds[i + 1]];
		}

		pathLength += distances[ds[ds.length - 1]][ds[0]];

		return pathLength;
	}
}
