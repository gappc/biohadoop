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
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskId;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrapper;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.WorkerData;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;
import at.ac.uibk.dps.biohadoop.utils.convert.ConversionException;
import at.ac.uibk.dps.biohadoop.utils.convert.MessageConverter;

public class UnifiedRestWorker<R, T, S> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultRestWorker.class);

	private final Map<String, WorkerData<R, T, S>> workerData = new ConcurrentHashMap<>();
	private final String path;

	private int logSteps = 1000;

	public UnifiedRestWorker(String className) throws WorkerException {
		path = WorkerInitializer.getRestPath(className);
	}

	public void run(String host, int port) throws WorkerException {
		String url = "http://" + host + ":" + port + "/rs/" + path;
		Client client = ClientBuilder.newClient();

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		try {
			Message<ClassNameWrapper<T>> inputMessage = receive(client, url
					+ "/workinit");

			while (inputMessage.getType() != MessageType.SHUTDOWN) {
				performanceLogger.step(LOG);

				Task<ClassNameWrapper<T>> task = inputMessage.getTask();
				String classString = task.getData().getClassName();

				WorkerData<R, T, S> workerEntry = getWorkerData(classString,
						client, url);

				RemoteExecutable<R, T, S> remoteExecutable = workerEntry
						.getRemoteExecutable();
				R initalData = workerEntry.getInitialData();
				T data = task.getData().getWrapped();

				S result = remoteExecutable.compute(data, initalData);

				Message<ClassNameWrapper<S>> outputMessage = createMessage(
						task.getTaskId(), classString, result);
				inputMessage = sendAndReceive(outputMessage, client, url
						+ "/work");
			}
			LOG.info("Got shutdown");
		} catch (IOException | ProcessingException e) {
			throw new WorkerException("Could not communicate with " + host
					+ ":" + port, e);
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

			Message<ClassNameWrapper<R>> registrationMessage = MessageConverter
					.getMessageForMethod(dataString, "getInitalData", -1);
			R initialData = registrationMessage.getTask().getData()
					.getWrapped();
			workerEntry = new WorkerData<R, T, S>(remoteExecutable, initialData);

			workerData.put(classString, workerEntry);
		}
		return workerEntry;
	}

	public Message<ClassNameWrapper<S>> createMessage(TaskId taskId,
			String classString, S data) {
		ClassNameWrapper<S> wrapper = new ClassNameWrapper<>(classString, data);
		Task<ClassNameWrapper<S>> responseTask = new Task<>(taskId, wrapper);
		return new Message<>(MessageType.WORK_REQUEST, responseTask);
	}

	private Message<ClassNameWrapper<T>> receive(Client client, String url)
			throws ConversionException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON).get();

		String dataString = response.readEntity(String.class);
		return MessageConverter.getMessageForMethod(dataString, "compute", 0);
	}

	private Message<ClassNameWrapper<T>> sendAndReceive(Message<?> message,
			Client client, String url) throws IOException, ConversionException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(message, MediaType.APPLICATION_JSON));

		String dataString = response.readEntity(String.class);
		return MessageConverter.getMessageForMethod(dataString, "compute", 0);
	}
}
