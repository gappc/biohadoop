package at.ac.uibk.dps.biohadoop.worker;

import java.io.IOException;
import java.net.URI;
import java.util.List;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.GaResult;
import at.ac.uibk.dps.biohadoop.ga.GaTask;
import at.ac.uibk.dps.biohadoop.rs.GaResource;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketGaTaskDecoder;
import at.ac.uibk.dps.biohadoop.websocket.registration.Registration;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = WebSocketGaTaskDecoder.class)
public class WebSocketGaClient {

	private static final Logger logger = LoggerFactory
			.getLogger(GaResource.class);

	private CountDownLatch latch = new CountDownLatch(1);
	private double[][] distances;
	private long start;

	private int counter = 0;

	public WebSocketGaClient() {
	}

	public WebSocketGaClient(URI uri, Registration registration)
			throws DeploymentException, IOException, InterruptedException {

		this.start = System.currentTimeMillis();
		this.distances = convertDistances(registration.getData());
		WebSocketContainer container = ContainerProvider
				.getWebSocketContainer();
		container.connectToServer(this, uri);
		latch.await();
	}

	private double[][] convertDistances(Object data) {
		List<List<Double>> input = (List<List<Double>>) data;
		double[][] result = new double[input.size()][input.size()];
		for (int i = 0; i < input.size(); i++) {
			for (int j = i; j < input.size(); j++) {
				result[i][j] = input.get(i).get(j);
				result[j][i] = input.get(i).get(j);
			}
		}
		return result;
	}

	@OnOpen
	public void onOpen(Session session) throws IOException {
		logger.debug("WebSocketGaClient OPENED " + session);
		session.getBasicRemote().sendText("{\"slot\": -1, \"result\": -1}");
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) throws IOException {
		logger.debug("WebSocketGaClient CLOSED");
	}

	@OnMessage
	public GaResult onMessage(GaTask task) {
		counter++;
		if (counter % 1000 == 0) {
			logger.debug("WebSocketGaClient Received Message: "
					+ (System.currentTimeMillis() - start) + "ms");
			this.start = System.currentTimeMillis();
			counter = 0;
		}
		GaResult gaResult = new GaResult();
		gaResult.setSlot(task.getSlot());
		gaResult.setResult(fitness(distances, task.getGenome()));
		gaResult.setId(task.getId());
		return gaResult;
	}

	// @OnMessage
	// public String onMessage(String message) throws JsonParseException,
	// JsonMappingException, IOException {
	// counter++;
	// if (counter % 1000 == 0) {
	// System.out.println((System.currentTimeMillis() - start));
	// this.start = System.currentTimeMillis();
	// counter = 0;
	// }
	// return res;
	// }

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
