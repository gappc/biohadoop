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
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.MessageType;
import at.ac.uibk.dps.biohadoop.websocket.MessageDecoder;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = MessageDecoder.class)
public class WebSocketWorker {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WebSocketWorker.class);

	private CountDownLatch latch = new CountDownLatch(1);
	private double[][] distances;
	private long startTime = System.currentTimeMillis();
	private int counter = 0;
	private int logSteps = 1000;
	private ObjectMapper om = new ObjectMapper();

	public static void main(String[] args) throws Exception {
		LOGGER.info("############# {} started ##############",
				WebSocketWorker.class.getSimpleName());
		LOGGER.debug("args.length: " + args.length);
		for (String s : args) {
			LOGGER.debug(s);
		}

		String masterHostname = args[0];
		String url = "ws://" + masterHostname + ":30000/websocket/ga";

		LOGGER.info(
				"############# WebSocket client calls url: {} #############",
				url);
		new WebSocketWorker(URI.create(url));
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

	@OnOpen
	public void onOpen(Session session) throws IOException {
		LOGGER.info("Opened connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) throws IOException {
		LOGGER.info("Closed connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		LOGGER.info("############# {} stopped #############",
				WebSocketWorker.class.getSimpleName());
	}

	@OnMessage
	public Message onMessage(Message message, Session session)
			throws IOException {
		counter++;
		if (counter % logSteps == 0) {
			long endTime = System.currentTimeMillis();
			LOGGER.info("{}ms for last {} computations", endTime - startTime,
					logSteps);
			startTime = System.currentTimeMillis();
			counter = 0;
		}

		LOGGER.debug("Received message from URI {} and sessionId {}: {}",
				session.getRequestURI(), session.getId(), message);

		MessageType messageType = MessageType.NONE;

		if (message.getType() == MessageType.REGISTRATION_RESPONSE) {
			LOGGER.debug("Registration successful for URI {} and sessionId {}",
					session.getRequestURI(), session.getId());
			Task<List<List<Double>>> task = om.convertValue(message.getPayload(),
					Task.class);

			List<List<Double>> inputDistances = task.getData();
			convertDistances(inputDistances);

			messageType = MessageType.WORK_INIT_REQUEST;
			return new Message(messageType, null);
		}

		if (message.getType() == MessageType.WORK_INIT_RESPONSE
				|| message.getType() == MessageType.WORK_RESPONSE) {
			LOGGER.debug("{} for URI {} and sessionId {}", message.getType(),
					session.getRequestURI(), session.getId());

			Task<List<Integer>> inputTask = om.convertValue(message.getPayload(),
					Task.class);
			Task<Double> response = computeResult(inputTask);

			return new Message<Double>(MessageType.WORK_REQUEST, response);
		}
		if (message.getType() == MessageType.SHUTDOWN) {
			LOGGER.info("Got SHUTDOWN message, now shutting down");
			session.close();
			latch.countDown();
		}
		System.out.println("!!!!!!!! SHOULD NOT COME HERE !!!!!!!");
		return new Message(messageType, null);
	}

	@OnError
	public void onError(Session session, Throwable t) {
		LOGGER.error("Error for URI {}, sessionId={}", session.getRequestURI(),
				session.getId(), t);
		latch.countDown();
	}
	
	private void convertDistances(List<List<Double>> inputDistances) {
		int length1 = inputDistances.size();
		int length2 = length1 != 0 ? inputDistances.get(0).size() : 0;
		distances = new double[length1][length2];
		for (int i = 0; i < length1; i++) {
			for (int j = i; j < length2; j++) {
				distances[i][j] = inputDistances.get(i).get(j);
				distances[j][i] = inputDistances.get(j).get(i);
			}
		}
	}

	private Task<Double> computeResult(Task<List<Integer>> task) {
		int[] data = Ints.toArray(task.getData());
		double fitness = GaFitness.computeFitness(distances, data);
		Task<Double> response = new Task<Double>(task.getTaskId(), fitness);
		return response;
	}
}
