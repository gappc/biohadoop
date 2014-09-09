package at.ac.uibk.dps.biohadoop.communication.worker;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.communication.ComputeException;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskId;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;
import at.ac.uibk.dps.biohadoop.utils.convert.ConversionException;
import at.ac.uibk.dps.biohadoop.utils.convert.MessageConverter;

public class RestWorker<R, T, S> implements Worker {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestWorker.class);

	private final Map<String, WorkerData<R, T, S>> workerData = new ConcurrentHashMap<>();
	private WorkerParameters parameters;
	private String path;

	private int logSteps = 1000;

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

	@Override
	public void start() throws WorkerException, ConnectionRefusedException {
		String url = "http://" + parameters.getHost() + ":"
				+ parameters.getPort() + "/rs/" + path;
		// TODO add timeout handling
		Client client = ClientBuilder.newClient();

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		try {
			Message<T> inputMessage = receive(client, url + "/workinit");

			while (inputMessage.getType() != MessageType.SHUTDOWN) {
				performanceLogger.step(LOG);

				ClassNameWrappedTask<T> task = (ClassNameWrappedTask<T>) inputMessage
						.getTask();
				String classString = task.getClassName();

				WorkerData<R, T, S> workerEntry = getWorkerData(task, client,
						url);

				RemoteExecutable<R, T, S> remoteExecutable = workerEntry
						.getRemoteExecutable();
				R initalData = workerEntry.getInitialData();
				T data = task.getData();

				S result = remoteExecutable.compute(data, initalData);

				Message<S> outputMessage = createMessage(task.getTaskId(),
						classString, result);
				inputMessage = sendAndReceive(outputMessage, client, url
						+ "/work");
			}
			LOG.info("Got shutdown");
		} catch (IOException | ProcessingException e) {
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

	private WorkerData<R, T, S> getWorkerData(ClassNameWrappedTask<T> task,
			Client client, String baseUrl) throws ClassNotFoundException,
			IOException, ConversionException, InstantiationException,
			IllegalAccessException {
		String classString = task.getClassName();
		WorkerData<R, T, S> workerEntry = workerData.get(classString);
		if (workerEntry == null) {
			Class<? extends RemoteExecutable<R, T, S>> className = (Class<? extends RemoteExecutable<R, T, S>>) Class
					.forName(classString);
			RemoteExecutable<R, T, S> remoteExecutable = className
					.newInstance();

			String url = baseUrl + "/initialdata/" + classString + "/"
					+ task.getTaskId();
			Response response = client.target(url)
					.request(MediaType.APPLICATION_JSON).get();
			String dataString = response.readEntity(String.class);

			Message<R> registrationMessage = MessageConverter
					.getTypedMessageForMethod(dataString, "compute", 1);
			R initialData = registrationMessage.getTask().getData();
			workerEntry = new WorkerData<R, T, S>(remoteExecutable, initialData);

			workerData.put(classString, workerEntry);
		}
		return workerEntry;
	}

	public Message<S> createMessage(TaskId taskId, String classString, S data) {
		Task<S> task = new ClassNameWrappedTask<>(taskId, data, classString);
		return new Message<>(MessageType.WORK_REQUEST, task);
	}

	private Message<T> receive(Client client, String url)
			throws ConversionException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON).get();

		String dataString = response.readEntity(String.class);
		return MessageConverter.getTypedMessageForMethod(dataString, "compute",
				0);
	}

	private Message<T> sendAndReceive(Message<?> message, Client client,
			String url) throws IOException, ConversionException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(message, MediaType.APPLICATION_JSON));

		String dataString = response.readEntity(String.class);
		return MessageConverter.getTypedMessageForMethod(dataString, "compute",
				0);
	}

}