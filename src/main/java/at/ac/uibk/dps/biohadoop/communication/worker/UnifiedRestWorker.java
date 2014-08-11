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

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.deletable.DefaultRestWorker;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskId;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.WorkerData;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;
import at.ac.uibk.dps.biohadoop.utils.convert.ConversionException;
import at.ac.uibk.dps.biohadoop.utils.convert.MessageConverter;

public class UnifiedRestWorker<R, T, S> implements WorkerEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultRestWorker.class);

	private final Map<String, WorkerData<R, T, S>> workerData = new ConcurrentHashMap<>();
	private WorkerParameters parameters;
	private String path;

	private int logSteps = 1000;

	@Override
	public void configure(String[] args) throws WorkerException {
		parameters = WorkerParameters.getParameters(args);
		path = WorkerInitializer.getRestPath(parameters.getRemoteExecutable());
	}

	@Override
	public void start() throws WorkerException {
		String url = "http://" + parameters.getHost() + ":"
				+ parameters.getPort() + "/rs/" + path;
		Client client = ClientBuilder.newClient();

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		try {
			Message<T> inputMessage = receive(client, url
					+ "/workinit");

			while (inputMessage.getType() != MessageType.SHUTDOWN) {
				performanceLogger.step(LOG);

				ClassNameWrappedTask<T> task = (ClassNameWrappedTask<T>)inputMessage.getTask();
				String classString = task.getClassName();

				WorkerData<R, T, S> workerEntry = getWorkerData(classString,
						client, url);

				RemoteExecutable<R, T, S> remoteExecutable = workerEntry
						.getRemoteExecutable();
				R initalData = workerEntry.getInitialData();
				T data = task.getData();

				S result = remoteExecutable.compute(data, initalData);

				Message<S> outputMessage = createMessage(
						task.getTaskId(), classString, result);
				inputMessage = sendAndReceive(outputMessage, client, url
						+ "/work");
			}
			LOG.info("Got shutdown");
		} catch (IOException | ProcessingException e) {
			throw new WorkerException("Could not communicate with "
					+ parameters.getHost() + ":" + parameters.getPort(), e);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | ConversionException e) {
			throw new WorkerException(e);
		}
	}

	private WorkerData<R, T, S> getWorkerData(String classString,
			Client client, String baseUrl) throws ClassNotFoundException,
			IOException, ConversionException, InstantiationException,
			IllegalAccessException {
		WorkerData<R, T, S> workerEntry = workerData.get(classString);
		if (workerEntry == null) {
			Class<? extends RemoteExecutable<R, T, S>> className = (Class<? extends RemoteExecutable<R, T, S>>) Class
					.forName(classString);
			RemoteExecutable<R, T, S> remoteExecutable = className
					.newInstance();

			String url = baseUrl + "/initialdata/"
					+ className.getCanonicalName();
			Response response = client.target(url)
					.request(MediaType.APPLICATION_JSON).get();
			String dataString = response.readEntity(String.class);

			Message<R> registrationMessage = MessageConverter
					.getTypedMessageForMethod(dataString, "getInitalData", -1);
			R initialData = registrationMessage.getTask().getData();
			workerEntry = new WorkerData<R, T, S>(remoteExecutable, initialData);

			workerData.put(classString, workerEntry);
		}
		return workerEntry;
	}

	public Message<S> createMessage(TaskId taskId,
			String classString, S data) {
		Task<S> task = new ClassNameWrappedTask<>(taskId, data, classString);
		return new Message<>(MessageType.WORK_REQUEST, task);
	}

	private Message<T> receive(Client client, String url)
			throws ConversionException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON).get();

		String dataString = response.readEntity(String.class);
		return MessageConverter.getTypedMessageForMethod(dataString, "compute", 0);
	}

	private Message<T> sendAndReceive(Message<?> message,
			Client client, String url) throws IOException, ConversionException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(message, MediaType.APPLICATION_JSON));

		String dataString = response.readEntity(String.class);
		return MessageConverter.getTypedMessageForMethod(dataString, "compute", 0);
	}

}
