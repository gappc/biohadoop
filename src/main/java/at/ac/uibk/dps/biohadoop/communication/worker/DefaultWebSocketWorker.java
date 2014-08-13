package at.ac.uibk.dps.biohadoop.communication.worker;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
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

import at.ac.uibk.dps.biohadoop.communication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskId;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class DefaultWebSocketWorker<R, T, S> implements WorkerEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultWebSocketWorker.class);

	private static final int connectionTimeout = 5000;

	private final Map<String, WorkerData<R, T, S>> workerDatas = new ConcurrentHashMap<>();
	private final CountDownLatch latch = new CountDownLatch(1);
	private final AtomicBoolean forceShutdown = new AtomicBoolean(true);
	private final PerformanceLogger performanceLogger = new PerformanceLogger(
			System.currentTimeMillis(), 0, 1000);

	private WorkerParameters parameters;
	private String path;
	private Message<T> oldMessage;

	public DefaultWebSocketWorker() {
	}

	@Override
	public String buildLaunchArguments(WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException {
		return ParameterConstructor.resolveHttpParameter(workerConfiguration);
	}

	@Override
	public void configure(String[] args) throws WorkerException {
		parameters = WorkerParameters.getParameters(args);
		path = PathConstructor.getWebSocketPath(parameters
				.getRemoteExecutable());
	}

	public void start() throws WorkerException {
		String url = "ws://" + parameters.getHost() + ":"
				+ parameters.getPort() + "/websocket/" + path;
		try {
			configureForceShutdown();
			WebSocketContainer container = ContainerProvider
					.getWebSocketContainer();
			Session session = container.connectToServer(this, URI.create(url));
			forceShutdown.set(false);
			workInit(session);
			latch.await();
		} catch (DeploymentException e) {
			throw new WorkerException("Could not deploy WebSocket", e);
		} catch (IOException e) {
			throw new WorkerException("Could not communicate with "
					+ parameters.getHost() + ":" + parameters.getPort(), e);
		} catch (EncodeException e) {
			throw new WorkerException("Could not encode data", e);
		} catch (InterruptedException e) {
			throw new WorkerException(
					"Error while waiting for worker to finish", e);
		}

	}

	private void configureForceShutdown() throws InterruptedException {
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.submit(new Callable<Object>() {
			@Override
			public Object call() throws WorkerException {
				try {
					Thread.sleep(connectionTimeout);
					if (forceShutdown.get()) {
						// TODO remove comment
						LOG.error("Forcing shutdown due to initial connection timeout");
						System.exit(1);
//						throw new WorkerException(
//								"Forcing shutdown due to initial connection timeout");
					}
				} catch (InterruptedException e) {
					// TODO remove comment
					LOG.error(
							"Got interrupted while waiting for connection timeout",
							e);
					System.exit(1);
//					throw new WorkerException(
//							"Got interrupted while waiting for connection timeout",
//							e);
				}
				return null;
			}
		});
		executorService.shutdown();
	}

	private void workInit(Session session) throws IOException, EncodeException {
		Message<?> message = new Message<Object>(MessageType.WORK_INIT_REQUEST,
				null);
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
	public Message<?> onMessage(Message<T> inputMessage, Session session)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		performanceLogger.step(LOG);

		LOG.debug("Received message from URI {} and sessionId {}: {}",
				session.getRequestURI(), session.getId(), inputMessage);

		if (inputMessage.getType() == MessageType.SHUTDOWN) {
			LOG.info("############# {} Worker stopped ###############",
					DefaultWebSocketWorker.class.getSimpleName());
			session.close();
			return null;
		}

		ClassNameWrappedTask<T> task = ((ClassNameWrappedTask<T>) inputMessage
				.getTask());
		String classString = task.getClassName();

		if (inputMessage.getType() == MessageType.REGISTRATION_RESPONSE) {
			Class<? extends RemoteExecutable<R, T, S>> className = (Class<? extends RemoteExecutable<R, T, S>>) Class
					.forName(classString);
			RemoteExecutable<R, T, S> remoteExecutable = className
					.newInstance();
			// Need conversion here as return type is none of R, T, S
			WorkerData<R, T, S> workerEntry = new WorkerData<R, T, S>(
					remoteExecutable, (R) task.getData());
			workerDatas.put(classString, workerEntry);
			inputMessage = oldMessage;
			task = (ClassNameWrappedTask<T>) inputMessage.getTask();
		}

		WorkerData<R, T, S> workerData = workerDatas.get(classString);
		if (workerData == null) {
			oldMessage = inputMessage;
			Task<T> intialTask = new ClassNameWrappedTask<>(null, null,
					classString);
			return new Message<>(MessageType.REGISTRATION_REQUEST, intialTask);
		}

		if (inputMessage.getType() == MessageType.WORK_INIT_RESPONSE
				|| inputMessage.getType() == MessageType.WORK_RESPONSE) {
			LOG.debug("{} for URI {} and sessionId {}", inputMessage.getType(),
					session.getRequestURI(), session.getId());

			T data = task.getData();

			RemoteExecutable<R, T, S> remoteExecutable = workerData
					.getRemoteExecutable();
			R initialData = workerData.getInitialData();

			S result = remoteExecutable.compute(data, initialData);

			Message<?> outputMessage = createMessage(task.getTaskId(),
					classString, result);

			return outputMessage;
			// return new Message<S>(MessageType.WORK_REQUEST, responseTask);
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

	public Message<?> createMessage(TaskId taskId, String classString, S data) {
		ClassNameWrappedTask<?> task = new ClassNameWrappedTask<>(taskId, data,
				classString);
		return new Message<>(MessageType.WORK_REQUEST, task);
	}

}
