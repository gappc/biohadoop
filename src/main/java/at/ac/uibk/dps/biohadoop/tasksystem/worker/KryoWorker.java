package at.ac.uibk.dps.biohadoop.tasksystem.worker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.kryo.KryoObjectRegistration;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskTypeId;
import at.ac.uibk.dps.biohadoop.utils.KryoRegistrator;

import com.esotericsoftware.kryo.Kryo;

public class KryoWorker<R, T, S> implements Worker {

	private static final Logger LOG = LoggerFactory.getLogger(KryoWorker.class);

	private final Map<TaskTypeId, WorkerData<R, T, S>> workerDatas = new ConcurrentHashMap<>();

	private WorkerParameters parameters;
	private int logSteps = 1000;
	private CountDownLatch latch = new CountDownLatch(1);

	private Message<T> oldMessage;

//	@Override
//	public String buildLaunchArguments(WorkerConfiguration workerConfiguration)
//			throws WorkerLaunchException {
//		return ParameterConstructor.resolveParameter(workerConfiguration,
//				Environment.KRYO_SOCKET_HOST, Environment.KRYO_SOCKET_PORT);
//	}
//
//	@Override
//	public void configure(String[] args) throws WorkerException {
//		parameters = WorkerParameters.getParameters(args);
//	}

	@Override
	public void start(String host, int port) throws WorkerException, ConnectionRefusedException {
//		final Client client = new Client(64 * 1024, 64 * 1024);
//		client.start();
//		try {
//			client.connect(ConnectionProperties.CONNECTION_TIMEOUT,
//					parameters.getHost(), parameters.getPort());
//		} catch (IOException e) {
//			throw new ConnectionRefusedException("Could not communicate with "
//					+ parameters.getHost() + ":" + parameters.getPort(), e);
//		}
//
//		Kryo kryo = client.getKryo();
//		registerObjects(kryo);
//
//		final PerformanceLogger performanceLogger = new PerformanceLogger(
//				System.currentTimeMillis(), 0, logSteps);
//
//		// TODO SEVERE: how to handle errors?
//		client.addListener(new Listener() {
//			public void received(Connection connection, Object object) {
//				if (object instanceof Message) {
//					WorkerData<R, T, S> workerData = null;
//					try {
//						Message<T> inputMessage = (Message<T>) object;
//
//						performanceLogger.step(LOG);
//
//						if (isShutdown(inputMessage)) {
//							client.close();
//							return;
//						}
//
//						Task<T> task = inputMessage.getTask();
//						TaskTypeId taskTypeId = task.getTaskTypeId();
//
//						LOG.error(taskTypeId.toString());
//						
//						if (inputMessage.getType() == MessageType.REGISTRATION_RESPONSE) {
//							TaskConfiguration<R> taskConfiguration = (TaskConfiguration) task
//									.getData();
//							String asyncComputableClassName = taskConfiguration
//									.getAsyncComputableClassName();
//							try {
//								Class<? extends AsyncComputable<R, T, S>> asyncComputableClass = (Class<? extends AsyncComputable<R, T, S>>) Class
//										.forName(asyncComputableClassName);
//								AsyncComputable<R, T, S> asyncComputable = asyncComputableClass
//										.newInstance();
//								workerData = new WorkerData<>(asyncComputable,
//										taskConfiguration.getInitialData());
//							} catch (ClassNotFoundException
//									| InstantiationException
//									| IllegalAccessException e) {
//								LOG.error(
//										"Could not instanciate AsyncComputable class {}",
//										asyncComputableClassName, e);
//								return;
//							}
//
//							workerDatas.put(taskConfiguration.getTaskTypeId(),
//									workerData);
//							inputMessage = oldMessage;
//							task = inputMessage.getTask();
//						} else {
//							workerData = workerDatas.get(taskTypeId);
//						}
//
//						if (workerData == null) {
//							oldMessage = inputMessage;
//
//							Task<T> intialTask = new Task<>(task.getTaskId(),
//									null, null);
//
//							connection.sendTCP(new Message<>(
//									MessageType.REGISTRATION_REQUEST,
//									intialTask));
//							return;
//						}
//
//						if (inputMessage.getType() == MessageType.WORK_INIT_RESPONSE
//								|| inputMessage.getType() == MessageType.WORK_RESPONSE) {
//							LOG.debug("WORK_INIT_RESPONSE | WORK_RESPONSE");
//
//							T data = task.getData();
//
//							AsyncComputable<R, T, S> asyncComputable = workerData
//									.getAsyncComputable();
//							R initalData = workerData.getInitialData();
//							S result = asyncComputable
//									.compute(data, initalData);
//
//							Message<S> outputMessage = createMessage(
//									task.getTaskId(), task.getTaskTypeId(),
//									result);
//
//							connection.sendTCP(outputMessage);
//						}
//					} catch (ComputeException e) {
//						LOG.error("Error while computing result", e);
//					}
//				}
//			}
//		});

//		workInit(client);

		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new WorkerException(
					"Error while waiting for worker to finish", e);
		}
	}

	private void registerObjects(Kryo kryo) throws WorkerException {
		KryoObjectRegistration.registerDefaultObjects(kryo);
		Map<String, String> properties = Environment
				.getBiohadoopConfiguration().getGlobalProperties();
		if (properties != null) {
			String kryoRegistratorClassName = properties
					.get(KryoRegistrator.KRYO_REGISTRATOR);
			if (kryoRegistratorClassName != null) {
				LOG.info("Registering additional objects for Kryo serialization");
				try {
					KryoRegistrator kryoRegistrator = (KryoRegistrator) Class
							.forName(kryoRegistratorClassName).newInstance();

					KryoObjectRegistration.registerTypes(kryo,
							kryoRegistrator.getRegistrationObjects());
					KryoObjectRegistration.registerTypes(kryo, kryoRegistrator
							.getRegistrationObjectsWithSerializer());
				} catch (InstantiationException | IllegalAccessException
						| ClassNotFoundException e) {
					throw new WorkerException(
							"Could not register objects for Kryo serialization, KryoRegistrator="
									+ kryoRegistratorClassName, e);
				}
			}
		}
	}

//	private void workInit(Client client) {
//		Message<?> message = new Message<Object>(MessageType.WORK_INIT_REQUEST,
//				null);
//		client.sendTCP(message);
//	}
//
//	private Message<S> createMessage(TaskId taskId, TaskTypeId taskTypeId,
//			S data) {
//		Task<S> task = new Task<>(taskId, taskTypeId, data);
//		return new Message<>(MessageType.WORK_REQUEST, task);
//	}
//
//	private boolean isShutdown(Message<?> inputMessage) {
//		if (inputMessage.getType() == MessageType.SHUTDOWN) {
//			LOG.info("############# {} Worker stopped ###############",
//					KryoWorker.class.getSimpleName());
//			latch.countDown();
//			return true;
//		}
//		return false;
//	}

}
