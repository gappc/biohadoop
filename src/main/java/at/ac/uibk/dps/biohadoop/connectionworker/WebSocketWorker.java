package at.ac.uibk.dps.biohadoop.connectionworker;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.connection.WorkerParameter;
import at.ac.uibk.dps.biohadoop.endpoint.WorkerEndpoint;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.Task;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class WebSocketWorker<T, S> implements WorkerEndpoint<T, S>,
		WorkerParameter {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketWorker.class);

	private CountDownLatch latch = new CountDownLatch(1);
	private long startTime = System.currentTimeMillis();
	private int counter = 0;
	private int logSteps = 1000;
	private ObjectMapper om = new ObjectMapper();
	
	public abstract String getPath();

	@Override
	public String getWorkerParameters() {
		String hostname = Environment.get(Environment.HTTP_HOST);
		String port = Environment.get(Environment.HTTP_PORT);
		return hostname + " " + port;
	}

	@Override
	public void run(String host, int port) throws Exception {
		WebSocketContainer container = ContainerProvider
				.getWebSocketContainer();

		String path = getPath();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String url = "ws://" + host + ":" + port + "/websocket" + path;
		Session session = container.connectToServer(this, URI.create(url));
		register(session);

		latch.await();
	}

	private void register(Session session) throws EncodeException, IOException {
		Message<?> message = new Message<Object>(
				MessageType.REGISTRATION_REQUEST, null);
		session.getBasicRemote().sendObject(message);
	}

	@OnOpen
	public void onOpen(Session session) throws IOException {
		LOG.info("Opened connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) throws IOException {
		LOG.info("Closed connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		LOG.info("############# {} stopped #############",
				WebSocketWorker.class.getSimpleName());
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

		if (message.getType() == MessageType.REGISTRATION_RESPONSE) {
			LOG.debug("Registration successful for URI {} and sessionId {}",
					session.getRequestURI(), session.getId());

			Task<?> task = om.convertValue(
					message.getPayload(), Task.class);

			Object data = task.getData();
			readRegistrationObject(data);

			return new Message<Object>(MessageType.WORK_INIT_REQUEST, null);
		}

		if (message.getType() == MessageType.WORK_INIT_RESPONSE
				|| message.getType() == MessageType.WORK_RESPONSE) {
			LOG.debug("{} for URI {} and sessionId {}", message.getType(),
					session.getRequestURI(), session.getId());

			@SuppressWarnings("unchecked")
			Task<T> inputTask = om.convertValue(message.getPayload(),
					Task.class);

			S response = compute(inputTask.getData());
			Task<S> responseTask = new Task<S>(inputTask.getTaskId(), response);

			return new Message<S>(MessageType.WORK_REQUEST, responseTask);
		}
		if (message.getType() == MessageType.SHUTDOWN) {
			LOG.info("############# {} Worker stopped ###############",
					WebSocketWorker.class.getSimpleName());
			session.close();
			return null;
		}
		LOG.error("SHOULD NOT BE HERE");
		return null;
	}

	@OnError
	public void onError(Session session, Throwable t) {
		LOG.error("Error for URI {}, sessionId={}", session.getRequestURI(),
				session.getId(), t);
		latch.countDown();
	}
}
