package at.ac.uibk.dps.biohadoop.tasksystem.worker;

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

import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.tasksystem.AsyncComputable;
import at.ac.uibk.dps.biohadoop.tasksystem.ComputeException;
import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskId;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskTypeId;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;
import at.ac.uibk.dps.biohadoop.utils.convert.ConversionException;
import at.ac.uibk.dps.biohadoop.utils.convert.MessageConverter;

public class RestWorker<R, T, S> implements Worker {

	private static final Logger LOG = LoggerFactory.getLogger(RestWorker.class);

	private final Map<TaskTypeId, WorkerData<R, T, S>> workerDatas = new ConcurrentHashMap<>();
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
		path = parameters.getPipelineName();
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

				Task<T> task = inputMessage.getTask();

				WorkerData<R, T, S> workerEntry = getWorkerData(task, client,
						url);

				AsyncComputable<R, T, S> asyncComputable = workerEntry
						.getAsyncComputable();
				R initalData = workerEntry.getInitialData();
				T data = task.getData();

				S result = asyncComputable.compute(data, initalData);

				Message<S> outputMessage = createMessage(task.getTaskId(),
						task.getTaskTypeId(), result);
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

	private WorkerData<R, T, S> getWorkerData(Task<T> task, Client client,
			String baseUrl) throws ClassNotFoundException, IOException,
			ConversionException, InstantiationException, IllegalAccessException {
		TaskTypeId taskTypeId = task.getTaskTypeId();
		WorkerData<R, T, S> workerData = workerDatas.get(taskTypeId);
		if (workerData == null) {
			String url = baseUrl + "/initialdata/" + task.getTaskId();
			Response response = client.target(url)
					.request(MediaType.APPLICATION_JSON).get();
			String dataString = response.readEntity(String.class);

			Message<R> registrationMessage = MessageConverter
					.getTypedMessageForMethod(dataString, "compute", 1);

			TaskConfiguration<R> taskConfiguration = (TaskConfiguration<R>) registrationMessage
					.getTask().getData();

			Class<? extends AsyncComputable<R, T, S>> asyncComputableClass = (Class<? extends AsyncComputable<R, T, S>>) Class
					.forName(taskConfiguration.getAsyncComputableClassName());
			AsyncComputable<R, T, S> asyncComputable = asyncComputableClass
					.newInstance();

			workerData = new WorkerData<R, T, S>(asyncComputable,
					taskConfiguration.getInitialData());

			workerDatas.put(taskTypeId, workerData);
		}
		return workerData;
	}

	public Message<S> createMessage(TaskId taskId, TaskTypeId taskTypeId, S data) {
		Task<S> task = new Task<>(taskId, taskTypeId, data);
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