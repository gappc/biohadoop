package at.ac.uibk.dps.biohadoop.tasksystem.worker;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.tasksystem.ComputeException;
import at.ac.uibk.dps.biohadoop.tasksystem.ConnectionProperties;
import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.kryo.KryoObjectRegistration;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskId;
import at.ac.uibk.dps.biohadoop.utils.KryoRegistrator;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class KryoWorker<R, T, S> implements Worker {

	private static final Logger LOG = LoggerFactory
			.getLogger(KryoWorker.class);

	private final Map<String, WorkerData<R, T, S>> workerData = new ConcurrentHashMap<>();

	private WorkerParameters parameters;
	private int logSteps = 1000;
	private CountDownLatch latch = new CountDownLatch(1);

	private Message<T> oldMessage;

	@Override
	public String buildLaunchArguments(WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException {
		return ParameterConstructor.resolveParameter(workerConfiguration,
				Environment.KRYO_SOCKET_HOST, Environment.KRYO_SOCKET_PORT);
	}

	@Override
	public void configure(String[] args) throws WorkerException {
		parameters = WorkerParameters.getParameters(args);
	}

	@Override
	public void start() throws WorkerException, ConnectionRefusedException {
		final Client client = new Client(64 * 1024, 64 * 1024);
		client.start();
		try {
			client.connect(ConnectionProperties.CONNECTION_TIMEOUT,
					parameters.getHost(), parameters.getPort());
		} catch (IOException e) {
			throw new ConnectionRefusedException("Could not communicate with "
					+ parameters.getHost() + ":" + parameters.getPort(), e);
		}

		Kryo kryo = client.getKryo();
		registerObjects(kryo);

		final PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);

		// TODO SEVERE: how to handle errors?
		client.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if (object instanceof Message) {
					String classString = null;
					try {
						Message<T> inputMessage = (Message<T>) object;

						performanceLogger.step(LOG);

						if (inputMessage.getType() == MessageType.SHUTDOWN) {
							LOG.info(
									"############# {} Worker stopped ###############",
									KryoWorker.class.getSimpleName());
							client.close();
							latch.countDown();
							return;
						}

						ClassNameWrappedTask<T> task = (ClassNameWrappedTask<T>) inputMessage
								.getTask();
						classString = task.getClassName();

						if (inputMessage.getType() == MessageType.REGISTRATION_RESPONSE) {
							Class<? extends RemoteExecutable<R, T, S>> className = (Class<? extends RemoteExecutable<R, T, S>>) Class
									.forName(classString);
							RemoteExecutable<R, T, S> remoteExecutable = className
									.newInstance();

							WorkerData<R, T, S> workerEntry = new WorkerData<>(
									remoteExecutable, (R) task.getData());
							workerData.put(classString, workerEntry);
							inputMessage = oldMessage;
							task = (ClassNameWrappedTask<T>) inputMessage
									.getTask();
						}

						WorkerData<R, T, S> workerEntry = workerData
								.get(classString);
						if (workerEntry == null) {
							oldMessage = inputMessage;

							Task<T> intialTask = new ClassNameWrappedTask<>(
									task.getTaskId(), null, classString);

							connection.sendTCP(new Message<>(
									MessageType.REGISTRATION_REQUEST,
									intialTask));
							return;
						}

						if (inputMessage.getType() == MessageType.WORK_INIT_RESPONSE
								|| inputMessage.getType() == MessageType.WORK_RESPONSE) {
							LOG.debug("WORK_INIT_RESPONSE | WORK_RESPONSE");

							T data = task.getData();

							RemoteExecutable<R, T, S> remoteExecutable = workerEntry
									.getRemoteExecutable();
							R initalData = workerEntry.getInitialData();
							S result = remoteExecutable.compute(data,
									initalData);

							Message<S> outputMessage = createMessage(
									task.getTaskId(), classString, result);

							connection.sendTCP(outputMessage);
						}
					} catch (ClassNotFoundException | InstantiationException
							| IllegalAccessException e) {
						LOG.error(
								"Could not instanciate RemoteExecutable class {}",
								classString, e);
					} catch (ComputeException e) {
						LOG.error("Error while computing result", e);
					}
				}
			}
		});

		workInit(client);

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

	private void workInit(Client client) {
		Message<?> message = new Message<Object>(MessageType.WORK_INIT_REQUEST,
				null);
		client.sendTCP(message);
	}

	public Message<S> createMessage(TaskId taskId, String classString, S data) {
		ClassNameWrappedTask<S> task = new ClassNameWrappedTask<>(taskId, data,
				classString);
		return new Message<>(MessageType.WORK_REQUEST, task);
	}

}