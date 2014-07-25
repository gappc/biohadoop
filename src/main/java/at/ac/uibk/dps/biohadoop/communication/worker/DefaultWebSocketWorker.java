package at.ac.uibk.dps.biohadoop.communication.worker;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.master.Master;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketMaster;
import at.ac.uibk.dps.biohadoop.queue.Task;

import com.fasterxml.jackson.databind.ObjectMapper;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class DefaultWebSocketWorker<T, S> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultWebSocketWorker.class);

	private static final int connectionTimeout = 2000;

	private final CountDownLatch latch = new CountDownLatch(1);
	private final AtomicBoolean forceShutdown = new AtomicBoolean(true);

	private Worker<T, S> worker;
	private long startTime = System.currentTimeMillis();
	private int counter = 0;
	private int logSteps = 1000;
	private ObjectMapper om = new ObjectMapper();

	public DefaultWebSocketWorker() {
	}

	public DefaultWebSocketWorker(Class<? extends Worker<T, S>> workerClass)
			throws InstantiationException, IllegalAccessException {
		worker = workerClass.newInstance();
	}

	public void run(String host, int port) throws WorkerException {
		Class<? extends Master> master = ((WebSocketWorker) worker.getClass()
				.getAnnotation(WebSocketWorker.class)).master();
		String path = master.getAnnotation(WebSocketMaster.class).path();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String url = "ws://" + host + ":" + port + "/websocket" + path;
		try {
			configureForceShutdown();
			WebSocketContainer container = ContainerProvider
					.getWebSocketContainer();
			Session session = container.connectToServer(this, URI.create(url));
			forceShutdown.set(false);
			register(session);
			latch.await();
		} catch (DeploymentException e) {
			throw new WorkerException("Could not deploy WebSocket", e);
		} catch (IOException e) {
			throw new WorkerException("Could not communicate with " + host
					+ ":" + port, e);
		} catch (EncodeException e) {
			throw new WorkerException("Could not encode data", e);
		} catch (InterruptedException e) {
			throw new WorkerException(
					"Error while waiting for worker to finish", e);
		}

	}

	private void configureForceShutdown() {
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.submit(new Callable<Integer>() {
			@Override
			public Integer call() throws WorkerException {
				try {
					Thread.sleep(connectionTimeout);
					if (forceShutdown.get()) {
						LOG.error("Forcing shutdown due to initial connection timeout");
						System.exit(0);
					}
				} catch (InterruptedException e) {
					LOG.error(
							"Got interrupted while waiting for connection timeout",
							e);
				}
				return 0;
			}
		});
		executorService.shutdown();
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
				DefaultWebSocketWorker.class.getSimpleName());
		latch.countDown();
	}

	@OnMessage
	public Message<?> onMessage(Message<?> message, Session session)
			throws IOException, InstantiationException, IllegalAccessException {
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

			Task<?> task = om.convertValue(message.getPayload(), Task.class);

			Object data = task.getData();
			worker.readRegistrationObject(data);

			return new Message<Object>(MessageType.WORK_INIT_REQUEST, null);
		}

		if (message.getType() == MessageType.WORK_INIT_RESPONSE
				|| message.getType() == MessageType.WORK_RESPONSE) {
			LOG.debug("{} for URI {} and sessionId {}", message.getType(),
					session.getRequestURI(), session.getId());

			@SuppressWarnings("unchecked")
			Task<T> inputTask = om.convertValue(message.getPayload(),
					Task.class);

			S response = worker.compute(inputTask.getData());
			Task<S> responseTask = new Task<S>(inputTask.getTaskId(), response);

			return new Message<S>(MessageType.WORK_REQUEST, responseTask);
		}
		if (message.getType() == MessageType.SHUTDOWN) {
			LOG.info("############# {} Worker stopped ###############",
					DefaultWebSocketWorker.class.getSimpleName());
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