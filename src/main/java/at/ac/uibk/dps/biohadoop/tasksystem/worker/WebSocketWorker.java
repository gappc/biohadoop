package at.ac.uibk.dps.biohadoop.tasksystem.worker;

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

import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.tasksystem.AsyncComputable;
import at.ac.uibk.dps.biohadoop.tasksystem.ComputeException;
import at.ac.uibk.dps.biohadoop.tasksystem.ConnectionProperties;
import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.websocket.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.websocket.WebSocketEncoder;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskId;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskTypeId;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = WebSocketDecoder.class)
public class WebSocketWorker<R, T, S> implements Worker {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketWorker.class);

	private final Map<TaskTypeId, WorkerData<R, T, S>> workerDatas = new ConcurrentHashMap<>();
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
		path = parameters.getPipelineName();
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
		WorkerData<R, T, S> workerData = null;
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

			Task<T> task = inputMessage.getTask();
			TaskTypeId taskTypeId = task.getTaskTypeId();

			if (inputMessage.getType() == MessageType.REGISTRATION_RESPONSE) {
				TaskConfiguration<R> taskConfiguration = (TaskConfiguration) task
						.getData();
				String asyncComputableClassName = taskConfiguration
						.getAsyncComputableClassName();
				try {
					Class<? extends AsyncComputable<R, T, S>> asyncComputableClass = (Class<? extends AsyncComputable<R, T, S>>) Class
							.forName(asyncComputableClassName);
					AsyncComputable<R, T, S> asyncComputable = asyncComputableClass
							.newInstance();
					workerData = new WorkerData<R, T, S>(asyncComputable,
							taskConfiguration.getInitialData());
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException e) {
					LOG.error("Could not instanciate AsyncComputable class {}",
							asyncComputableClassName, e);
					// TODO what to do in case of error?
					return null;
				}
				workerDatas.put(taskConfiguration.getTaskTypeId(), workerData);
				inputMessage = oldMessage;
				task = inputMessage.getTask();
			} else {
				workerData = workerDatas.get(taskTypeId);
			}

			if (workerData == null) {
				oldMessage = inputMessage;
				Task<T> intialTask = new Task<>(task.getTaskId(), null, null);
				return new Message<>(MessageType.REGISTRATION_REQUEST,
						intialTask);
			}

			if (inputMessage.getType() == MessageType.WORK_INIT_RESPONSE
					|| inputMessage.getType() == MessageType.WORK_RESPONSE) {
				LOG.debug("{} for URI {} and sessionId {}",
						inputMessage.getType(), session.getRequestURI(),
						session.getId());

				T data = task.getData();

				AsyncComputable<R, T, S> asyncComputable = workerData
						.getAsyncComputable();
				R initialData = workerData.getInitialData();

				S result = asyncComputable.compute(data, initialData);

				Message<?> outputMessage = createMessage(task.getTaskId(),
						task.getTaskTypeId(), result);

				return outputMessage;
			}

			LOG.error("SHOULD NOT BE HERE");
			return null;
		} catch (IOException e) {
			LOG.error("Error during communication", e);
			throw new WorkerException("Error during communication", e);
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

	public Message<?> createMessage(TaskId taskId, TaskTypeId taskTypeId, S data) {
		Task<?> task = new Task<>(taskId, taskTypeId, data);
		return new Message<>(MessageType.WORK_REQUEST, task);
	}

}
