package at.ac.uibk.dps.biohadoop.ga.worker;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageDecoder;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = MessageDecoder.class)
public class WebSocketWorker {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WebSocketWorker.class);

	private CountDownLatch latch = new CountDownLatch(1);
	private double[][] distances;
	private long start = System.currentTimeMillis();
	private int counter = 0;
	private ObjectMapper om = new ObjectMapper();

	public static void main(String[] args) throws Exception {
		new WebSocketWorker(URI.create("ws://kleintroppl:30000/websocket/ga"));
	}

	public WebSocketWorker() {
	}

	public WebSocketWorker(URI uri) throws DeploymentException, IOException,
			InterruptedException, EncodeException {
		WebSocketContainer container = ContainerProvider
				.getWebSocketContainer();
		Session session = container.connectToServer(this, uri);
		register(session);

		latch.await();
	}

	private void register(Session session) throws EncodeException, IOException {
		Message message = new Message(MessageType.REGISTRATION_REQUEST, null);
		session.getBasicRemote().sendObject(message);
	}

	private double[][] convertDistances(Object data) {
		@SuppressWarnings("unchecked")
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
		LOGGER.info(
				"WebSocketWorker opened connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) throws IOException {
		LOGGER.info(
				"WebSocketWorker closed connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
	}

	@OnMessage
	public Message onMessage(Message message, Session session) {
		counter++;
		if (counter % 1000 == 0) {
			LOGGER.debug("WebSocketGaClient Received Message: "
					+ (System.currentTimeMillis() - start) + "ms");
			this.start = System.currentTimeMillis();
			counter = 0;
		}

		LOGGER.debug(
				"WebSocketWorker received message from URI {} and sessionId {}: {}",
				session.getRequestURI(), session.getId(), message);

		if (message.getType() == MessageType.REGISTRATION_RESPONSE) {
			LOGGER.debug(
					"WebSocketWorker registration successful for URI {} and sessionId {}",
					session.getRequestURI(), session.getId());
			return new Message(MessageType.WORK_INIT_REQUEST, null);
		}

		if (message.getType() == MessageType.WORK_INIT_RESPONSE) {
			LOGGER.debug(
					"WebSocketWorker WORK_INIT_RESPONSE for URI {} and sessionId {}",
					session.getRequestURI(), session.getId());

			@SuppressWarnings("unchecked")
			List<Object> data = (List<Object>) message.getData();
			distances = convertDistances(data.get(0));
			GaTask task = om.convertValue(data.get(1), GaTask.class);
			return new Message(MessageType.WORK_REQUEST, computeResult(task));
		}

		if (message.getType() == MessageType.WORK_RESPONSE) {
			LOGGER.debug(
					"WebSocketWorker WORK_RESPONSE for URI {} and sessionId {}",
					session.getRequestURI(), session.getId());
			GaTask task = om.convertValue(message.getData(), GaTask.class);

			return new Message(MessageType.WORK_REQUEST, computeResult(task));
		}

		throw new RuntimeException(
				"Could not identify Websocket server response");
	}

	private GaResult computeResult(GaTask task) {
		GaResult gaResult = new GaResult();
		gaResult.setSlot(task.getSlot());
		gaResult.setResult(GaFitness.computeFitness(distances, task.getGenome()));
		gaResult.setId(task.getId());
		return gaResult;
	}

	@OnError
	public void onError(Session session, Throwable t) {
		LOGGER.error("WebSocketWorker error for URI {}, sessionId={}",
				session.getRequestURI(), session.getId(), t);
	}
}
