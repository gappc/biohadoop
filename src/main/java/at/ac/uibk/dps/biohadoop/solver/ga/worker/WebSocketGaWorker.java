package at.ac.uibk.dps.biohadoop.solver.ga.worker;

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

import at.ac.uibk.dps.biohadoop.connection.WorkerConnection;
import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.connection.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.service.job.Task;
import at.ac.uibk.dps.biohadoop.service.job.remote.Message;
import at.ac.uibk.dps.biohadoop.service.job.remote.MessageType;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.GaFitness;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class WebSocketGaWorker implements WorkerConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketGaWorker.class);

	private CountDownLatch latch = new CountDownLatch(1);
	private double[][] distances;
	private long startTime = System.currentTimeMillis();
	private int counter = 0;
	private int logSteps = 1000;
	private ObjectMapper om = new ObjectMapper();

	public static void main(String[] args) throws Exception {
		LOG.info("############# {} started ##############",
				WebSocketGaWorker.class.getSimpleName());
		LOG.debug("args.length: " + args.length);
		for (String s : args) {
			LOG.debug(s);
		}

		String host = args[0];
		int port = Integer.valueOf(args[1]);
		String url = "ws://" + host + ":" + port + "/websocket/ga";

		LOG.info(
				"############# WebSocket client calls url: {} #############",
				url);
		new WebSocketGaWorker(URI.create(url));
	}
	
	public WebSocketGaWorker() {
	}

	@Override
	public String getWorkerParameters() {
		String hostname = Environment.get(Environment.HTTP_HOST);
		String port = Environment.get(Environment.HTTP_PORT);
		return hostname + " " + port;
	}
	
	public WebSocketGaWorker(URI uri) throws DeploymentException, IOException,
			InterruptedException, EncodeException {
		WebSocketContainer container = ContainerProvider
				.getWebSocketContainer();
		Session session = container.connectToServer(this, uri);
		register(session);

		latch.await();
	}

	private void register(Session session) throws EncodeException, IOException {
		Message<?> message = new Message<Object>(MessageType.REGISTRATION_REQUEST, null);
		session.getBasicRemote().sendObject(message);
	}

	@OnOpen
	public void onOpen(Session session) throws IOException {
		LOG.info("Opened connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) throws IOException {
		LOG.info("FUCK ONCLOSE ERROR HANDLING");
		LOG.info("Closed connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		LOG.info("############# {} stopped #############",
				WebSocketGaWorker.class.getSimpleName());
		latch.countDown();
	}

	@OnMessage
	public Message<?> onMessage(Message<?> message, Session session)
			throws IOException {
		counter++;
		if (counter % logSteps == 0) {
			long endTime = System.currentTimeMillis();
			LOG.info("{}ms for last {} computations", endTime - startTime,
					logSteps);
			startTime = System.currentTimeMillis();
			counter = 0;
		}

		LOG.debug("Received message from URI {} and sessionId {}: {}",
				session.getRequestURI(), session.getId(), message);

		MessageType messageType = MessageType.NONE;

		if (message.getType() == MessageType.REGISTRATION_RESPONSE) {
			LOG.debug("Registration successful for URI {} and sessionId {}",
					session.getRequestURI(), session.getId());
			
			@SuppressWarnings("unchecked")
			Task<List<List<Double>>> task = om.convertValue(message.getPayload(),
					Task.class);

			List<List<Double>> inputDistances = task.getData();
			convertDistances(inputDistances);

			messageType = MessageType.WORK_INIT_REQUEST;
			return new Message<Object>(messageType, null);
		}

		if (message.getType() == MessageType.WORK_INIT_RESPONSE
				|| message.getType() == MessageType.WORK_RESPONSE) {
			LOG.debug("{} for URI {} and sessionId {}", message.getType(),
					session.getRequestURI(), session.getId());

			@SuppressWarnings("unchecked")
			Task<List<Integer>> inputTask = om.convertValue(message.getPayload(),
					Task.class);
			Task<Double> response = computeResult(inputTask);

			return new Message<Double>(MessageType.WORK_REQUEST, response);
		}
		if (message.getType() == MessageType.SHUTDOWN) {
			LOG.info(
					"############# {} Worker stopped ###############",
					WebSocketGaWorker.class.getSimpleName());
			session.close();
		}
		LOG.error("SHOULD NOT BE HERE");
		return null;
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
		LOG.error("FUCK ERROR HANDLING");
		LOG.error("Error for URI {}, sessionId={}", session.getRequestURI(),
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
