package at.ac.uibk.dps.biohadoop.solver.ga.worker;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.WorkerConnection;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.service.job.Task;
import at.ac.uibk.dps.biohadoop.service.job.remote.Message;
import at.ac.uibk.dps.biohadoop.service.job.remote.MessageType;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.solver.ga.master.GaEndpointConfig;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Ints;

public class RestGaWorker implements WorkerConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestGaWorker.class);

	private static String className = Helper.getClassname(RestGaWorker.class);
	private int logSteps = 1000;
	private double[][] distances;

	public static void main(String[] args) throws InterruptedException {
		LOG.info("############# {} started ##############", className);
		LOG.debug("args.length: {}", args.length);
		for (String s : args) {
			LOG.debug(s);
		}

		String host = args[0];
		int port = Integer.valueOf(args[1]);

		LOG.info("############# {} client calls master at: {}:{} #############",
				className, host, port);
		new RestGaWorker(host, port);
		LOG.info("############# {} stopped #############", className);
	}
	
	public RestGaWorker() {
	}
	
	@Override
	public String getWorkerParameters() {
		String hostname = Environment.get(Environment.HTTP_HOST);
		String port = Environment.get(Environment.HTTP_PORT);
		return hostname + " " + port;
	}

	public RestGaWorker(String masterHostname, int port) {
		String url = "http://" + masterHostname + ":" + port + "/rs/ga/";
		Client client = ClientBuilder.newClient();
		ObjectMapper mapper = new ObjectMapper();

		Message<?> registrationMessage = receive(client, url + "registration");
		distances = mapper.convertValue(registrationMessage.getPayload()
				.getData(), double[][].class);

		Message<List<Integer>> inputMessage = this.<List<Integer>> receive(
				client, url + "workinit");

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (true) {
			performanceLogger.step(LOG);

			LOG.debug("{} WORK_RESPONSE", className);

			if (inputMessage.getType() == MessageType.SHUTDOWN) {
				break;
			}

			Task<List<Integer>> inputTask = inputMessage.getPayload();
			Task<Double> response = computeResult(inputTask);

			Message<Double> message = new Message<Double>(
					MessageType.WORK_REQUEST, response);
			inputMessage = this.<List<Integer>> sendAndReceive(message, client,
					url + "work");
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Message<T> receive(Client client, String url) {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON).get();

		return response.readEntity(Message.class);
	}

	@SuppressWarnings("unchecked")
	private <T> Message<T> sendAndReceive(Message<?> message, Client client,
			String url) {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(message, MediaType.APPLICATION_JSON));

		return response.readEntity(Message.class);
	}

	private Task<Double> computeResult(Task<List<Integer>> task) {
		int[] data = Ints.toArray(task.getData());
		double fitness = GaFitness.computeFitness(distances, data);
		Task<Double> response = new Task<Double>(task.getTaskId(), fitness);
		return response;
	}
}
