package at.ac.uibk.dps.biohadoop.tasksystem.worker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.tasksystem.AsyncComputable;
import at.ac.uibk.dps.biohadoop.tasksystem.ComputeException;
import at.ac.uibk.dps.biohadoop.tasksystem.ConnectionProperties;
import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskId;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskTypeId;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;
import at.ac.uibk.dps.biohadoop.utils.convert.ConversionException;

public class SocketWorker<R, T, S> implements Worker {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketWorker.class);

	private static String className = ClassnameProvider
			.getClassname(SocketWorker.class);

	private final Map<TaskTypeId, WorkerData<R, T, S>> workerData = new ConcurrentHashMap<>();
	private WorkerParameters parameters;
	private int logSteps = 1000;

	@Override
	public String buildLaunchArguments(WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException {
		return ParameterConstructor.resolveParameter(workerConfiguration,
				Environment.SOCKET_HOST, Environment.SOCKET_PORT);
	}

	@Override
	public void configure(String[] args) throws WorkerException {
		parameters = WorkerParameters.getParameters(args);
	}

	@Override
	public void start() throws WorkerException, ConnectionRefusedException {
		try {
			// TODO: implement timeout
			Socket clientSocket = new Socket(parameters.getHost(),
					parameters.getPort());
			clientSocket.setSoTimeout(ConnectionProperties.CONNECTION_TIMEOUT);
			ObjectOutputStream os = new ObjectOutputStream(
					new BufferedOutputStream(clientSocket.getOutputStream()));
			os.flush();
			ObjectInputStream is = new ObjectInputStream(
					new BufferedInputStream(clientSocket.getInputStream()));

			handleWork(is, os);

			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {
			throw new ConnectionRefusedException("Could not communicate with "
					+ parameters.getHost() + ":" + parameters.getPort(), e);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | ConversionException e) {
			throw new WorkerException(e);
		} catch (ComputeException e) {
			throw new WorkerException(
					"Error while computing result, stopping work", e);
		}
	}

	private void handleWork(ObjectInputStream is, ObjectOutputStream os)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, ConversionException, ComputeException {
		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		int counter = 0;

		Message<Object> requestMessage = new Message<Object>(
				MessageType.WORK_INIT_REQUEST, null);
		send(os, requestMessage);

		Message<T> inputMessage = receive(is);

		while (inputMessage.getType() != MessageType.SHUTDOWN) {
			performanceLogger.step(LOG);
			counter++;
			if (counter % 1000 == 0) {
				os.reset();
				counter = 0;
			}

			LOG.debug("{} WORK_RESPONSE", className);

			Task<T> task = inputMessage.getTask();

			WorkerData<R, T, S> workerEntry = getWorkerData(task, os, is);

			AsyncComputable<R, T, S> asyncComputable = workerEntry
					.getAsyncComputable();
			R initalData = workerEntry.getInitialData();
			T data = task.getData();

			S result = asyncComputable.compute(data, initalData);

			Message<S> outputMessage = createMessage(task.getTaskId(),
					task.getTaskTypeId(), result);

			send(os, outputMessage);

			inputMessage = receive(is);
		}
	}

	@SuppressWarnings("unchecked")
	private Message<T> receive(ObjectInputStream is) throws IOException,
			ClassNotFoundException {
		return (Message<T>) is.readUnshared();
	}

	private void send(ObjectOutputStream os, Message<?> message)
			throws IOException {
		os.writeUnshared(message);
		os.flush();
	}

	private WorkerData<R, T, S> getWorkerData(Task<T> task,
			ObjectOutputStream os, ObjectInputStream is)
			throws ClassNotFoundException, IOException, ConversionException,
			InstantiationException, IllegalAccessException {
		TaskTypeId taskTypeId = task.getTaskTypeId();
		WorkerData<R, T, S> workerEntry = workerData.get(taskTypeId);
		if (workerEntry == null) {
			Task<T> intialTask = new Task<>(task.getTaskId(), null, null);
			Message<?> registrationRequest = new Message<>(
					MessageType.REGISTRATION_REQUEST, intialTask);

			send(os, registrationRequest);
			Message<T> inputMessage = receive(is);

			TaskConfiguration<R> taskConfiguration = (TaskConfiguration<R>) inputMessage
					.getTask().getData();
			Class<? extends AsyncComputable<R, T, S>> asyncComputableClass = (Class<? extends AsyncComputable<R, T, S>>) Class
					.forName(taskConfiguration.getAsyncComputableClassName());
			AsyncComputable<R, T, S> asyncComputable = asyncComputableClass
					.newInstance();

			workerEntry = new WorkerData<>(asyncComputable, taskConfiguration.getInitialData());

			workerData.put(taskTypeId, workerEntry);
		}
		return workerEntry;
	}

	public Message<S> createMessage(TaskId taskId, TaskTypeId taskTypeId, S data) {
		Task<S> task = new Task<>(taskId, taskTypeId, data);
		return new Message<>(MessageType.WORK_REQUEST, task);
	}

}
