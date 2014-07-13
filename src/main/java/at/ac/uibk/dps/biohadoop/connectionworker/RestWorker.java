package at.ac.uibk.dps.biohadoop.connectionworker;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.connection.WorkerConnection;
import at.ac.uibk.dps.biohadoop.endpoint.WorkerEndpoint;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;

public abstract class RestWorker<T, S> implements WorkerEndpoint<T, S>, WorkerConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestWorker.class);
	private static final String CLASSNAME = Helper.getClassname(RestWorker.class);
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	private int logSteps = 1000;
	
	public abstract String getPath();
	public abstract TypeReference<Message<T>> getInputType();
	
	@Override
	public String getWorkerParameters() {
		String hostname = Environment.get(Environment.HTTP_HOST);
		String port = Environment.get(Environment.HTTP_PORT);
		return hostname + " " + port;
	}

	@Override
	public void run(String host, int port) throws Exception {
		String path = getPath();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		String url = "http://" + host + ":" + port + "/rs" + path;
		Client client = ClientBuilder.newClient();

		Message<?> registrationMessage = receiveRegistration(client, url + "/registration");
		Object data = registrationMessage.getPayload().getData();
		readRegistrationObject(data);

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
			
			S response = compute((T) inputTask.getData());

			Task<S> responseTask = new Task<S>(inputTask
					.getTaskId(), response);

			Message<S> message = new Message<S>(
					MessageType.WORK_REQUEST, responseTask);
			inputMessage = sendAndReceive(message, client,
					url + "/work");
		}
	}

	@SuppressWarnings("unchecked")
	private Message<T> receiveRegistration(Client client, String url)
			throws JsonParseException, JsonMappingException, IOException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON).get();
		return response.readEntity(Message.class);
	}
	
	private Message<T> receive(Client client, String url)
			throws JsonParseException, JsonMappingException, IOException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON).get();
		String dataString = response.readEntity(String.class);
		return objectMapper.readValue(dataString, getInputType());
	}

	private Message<T> sendAndReceive(Message<?> message, Client client,
			String url) throws JsonParseException, JsonMappingException, IOException {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(message, MediaType.APPLICATION_JSON));
		
		String dataString = response.readEntity(String.class);
		return objectMapper.readValue(dataString, getInputType());
	}
}
