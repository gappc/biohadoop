package at.ac.uibk.dps.biohadoop.communication.worker;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.websocket.ClientEndpoint;
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

import at.ac.uibk.dps.biohadoop.communication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.communication.ComputeException;
import at.ac.uibk.dps.biohadoop.communication.ConnectionProperties;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.adapter.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.communication.adapter.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskId;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class WebSocketWorker<R, T, S> implements Worker {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketWorker.class);

	private final Map<String, WorkerData<R, T, S>> workerDatas = new ConcurrentHashMap<>();
	private final CountDownLatch latch = new CountDownLatch(1);
	private final PerformanceLogger performanceLogger = new PerformanceLogger(
			System.currentTimeMillis(), 0, 1000);

	private WorkerParameters parameters;
	private String path;
	private Message<T> oldMessage;

	public WebSocketWorker() {
	}

	@Override
	public String buildLaunchArguments(WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException {
		return ParameterConstructor.resolveHttpParameter(workerConfiguration);
	}

	@Override
	public void configure(String[] args) throws WorkerException {
		parameters = WorkerParameters.getParameters(args);
		path = parameters.getSettingName();
	}

	public void start() throws WorkerException, ConnectionRefusedException {
		final String url = "ws://" + parameters.getHost() + ":"
				+ parameters.getPort() + "/websocket/" + path;
		try {
			final WebSocketContainer container = ContainerProvider
					.getWebSocketContainer();
			container
					.setDefaultMaxSessionIdleTimeout(ConnectionProperties.CONNECTION_TIMEOUT);

			final WebSocketWorker<R, T, S> self = this;
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<Session> future = executor.submit(new Callable<Session>() {
				@Override
				public Session call() throws Exception {
					return container.connectToServer(self, URI.create(url));
				}
			});
			Session session = future.get(
					ConnectionProperties.CONNECTION_TIMEOUT,
					TimeUnit.MILLISECONDS);
			workInit(session);
			latch.await();
		} catch (ExecutionException e) {
			throw new WorkerException("Error while connecting to server", e);
		} catch (TimeoutException e) {
			throw new ConnectionRefusedException(
					"Connecting to server timed out after "
							+ ConnectionProperties.CONNECTION_TIMEOUT + "ms", e);
		} catch (IOException e) {
			throw new ConnectionRefusedException("Could not communicate with "
					+ parameters.getHost() + ":" + parameters.getPort(), e);
		} catch (EncodeException e) {
			throw new WorkerException("Could not encode data", e);
		} catch (InterruptedException e) {
			throw new WorkerException(
					"Error while waiting for worker to finish", e);
		}

	}

	private void workInit(Session session) throws IOException, EncodeException {
		Message<?> message = new Message<Object>(MessageType.WORK_INIT_REQUEST,
				null);
		session.getBasicRemote().sendObject(message);
	}

	@OnOpen
	public void onOpen(Session session) {
		LOG.info("Opened connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		LOG.info("Closed connection to URI {}, sessionId={}",
				session.getRequestURI(), session.getId());
		LOG.info("############# {} stopped #############",
				WebSocketWorker.class.getSimpleName());
		latch.countDown();
	}

	@OnMessage
	public Message<?> onMessage(Message<T> inputMessage, Session session)
			throws WorkerException {
		String classString = null;
		try {
			performanceLogger.step(LOG);

			LOG.debug("Received message from URI {} and sessionId {}: {}",
					session.getRequestURI(), session.getId(), inputMessage);

			if (inputMessage.getType() == MessageType.SHUTDOWN) {
				LOG.info("############# {} Worker stopped ###############",
						WebSocketWorker.class.getSimpleName());
				session.close();
				return null;
			}

			ClassNameWrappedTask<T> task = ((ClassNameWrappedTask<T>) inputMessage
					.getTask());
			classString = task.getClassName();

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
				Task<T> intialTask = new ClassNameWrappedTask<>(
						task.getTaskId(), null, classString);
				return new Message<>(MessageType.REGISTRATION_REQUEST,
						intialTask);
			}

			if (inputMessage.getType() == MessageType.WORK_INIT_RESPONSE
					|| inputMessage.getType() == MessageType.WORK_RESPONSE) {
				LOG.debug("{} for URI {} and sessionId {}",
						inputMessage.getType(), session.getRequestURI(),
						session.getId());

				T data = task.getData();

				RemoteExecutable<R, T, S> remoteExecutable = workerData
						.getRemoteExecutable();
				R initialData = workerData.getInitialData();

				S result = remoteExecutable.compute(data, initialData);

				Message<?> outputMessage = createMessage(task.getTaskId(),
						classString, result);

				return outputMessage;
			}

			LOG.error("SHOULD NOT BE HERE");
			return null;
		} catch (IOException e) {
			LOG.error("Error during communication", e);
			throw new WorkerException("Error during communication", e);
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			LOG.error("Could not instanciate RemoteExecutable class {}",
					classString, e);
			throw new WorkerException(
					"Could not instanciate RemoteExecutable class "
							+ classString, e);
		} catch (ComputeException e) {
			LOG.error("Error while computing result", e);
			throw new WorkerException("Error while computing result", e);
		}
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
