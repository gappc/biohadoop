package at.ac.uibk.dps.biohadoop.communication.worker;

import java.io.IOException;

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
import at.ac.uibk.dps.biohadoop.communication.master.rest2.RestMaster;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SuperRestWorker<T, S> {// implements WorkerParameter {

	private static final Logger LOG = LoggerFactory.getLogger(SuperRestWorker.class);
	private static final String CLASSNAME = ClassnameProvider
			.getClassname(SuperRestWorker.class);

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final SuperWorker<T, S> worker;

	private int logSteps = 1000;

	public SuperRestWorker(Class<? extends SuperWorker<T, S>> workerClass)
			throws InstantiationException, IllegalAccessException {
		worker = workerClass.newInstance();
	}
	
//	@Override
//	public String getWorkerParameters() {
//		String hostname = Environment.get(Environment.HTTP_HOST);
//		String port = Environment.get(Environment.HTTP_PORT);
//		return hostname + " " + port;
//	}

	public void run(String host, int port) throws WorkerException {
		String path = worker.getClass().getAnnotation(RestWorkerAnnotation.class).master().getAnnotation(RestMaster.class).path();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String url = "http://" + host + ":" + port + "/rs" + path;
		Client client = ClientBuilder.newClient();

		try {
			Message<?> registrationMessage = receiveRegistration(client, url
					+ "/registration");
			Object data = registrationMessage.getPayload().getData();
			
			worker.readRegistrationObject(data);

			Message<T> inputMessage = receive(client, url + "/workinit");

			PerformanceLogger performanceLogger = new PerformanceLogger(
					System.currentTimeMillis(), 0, logSteps);
			while (true) {
				performanceLogger.step(LOG);

				LOG.debug("{} WORK_RESPONSE", CLASSNAME);

				if (inputMessage.getType() == MessageType.SHUTDOWN) {
					LOG.info("Got shutdown");
					break;
				}

				Task<T> inputTask = inputMessage.getPayload();

				S response = worker.compute((T) inputTask.getData());

				Task<S> responseTask = new Task<S>(inputTask.getTaskId(),
						response);

				Message<S> message = new Message<S>(MessageType.WORK_REQUEST,
						responseTask);
				inputMessage = sendAndReceive(message, client, url + "/work");
			}
		} catch (IOException | ProcessingException e) {
			throw new WorkerException("Could not communicate with " + host + ":" + port,
					e);
		}
	}

	@SuppressWarnings("unchecked")
	private Message<T> receiveRegistration(Client client, String url)
			throws IOException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON).get();
		return response.readEntity(Message.class);
	}

	private Message<T> receive(Client client, String url) throws IOException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON).get();
		String dataString = response.readEntity(String.class);
		Class<?> receiveClass = worker.getClass().getAnnotation(RestWorkerAnnotation.class).receive();
		JavaType javaType = objectMapper.getTypeFactory().constructParametricType(Message.class, receiveClass);
		return objectMapper.readValue(dataString, javaType);
	}

	private Message<T> sendAndReceive(Message<?> message, Client client,
			String url) throws IOException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(message, MediaType.APPLICATION_JSON));

		String dataString = response.readEntity(String.class);
		
		Class<?> receiveClass = worker.getClass().getAnnotation(RestWorkerAnnotation.class).receive();
		JavaType javaType = objectMapper.getTypeFactory().constructParametricType(Message.class, receiveClass);
		return objectMapper.readValue(dataString, javaType);
	}
}
