package at.ac.uibk.dps.biohadoop.ga.worker;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;

public class RestGaWorker {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RestGaWorker.class);
	
	private String url;
	private Client client;
	private int logSteps = 1000;

	public static void main(String[] args) throws InterruptedException {
		LOGGER.info("!!!!!!!GaWorker args[0]: " + args[0]);
		RestGaWorker gaWorker = new RestGaWorker();
		gaWorker.runClient(args);
	}

	private void runClient(String[] args) {
		try {
			long startTime = System.currentTimeMillis();
			LOGGER.info("############# {} started ##############", RestGaWorker.class.getSimpleName());
			LOGGER.debug("args.length: " + args.length);
			for (String s : args) {
				LOGGER.debug(s);
			}

			String masterHostname = args[0];
			url = "http://" + masterHostname + ":30000/rs/ga";

			LOGGER.info("############# client calls url: {} #############", url);
			client = ClientBuilder.newClient();

			Message message = sendAndReceive(new Message(
					MessageType.REGISTRATION_REQUEST, null));

			if (message.getType() == MessageType.REGISTRATION_RESPONSE) {
				message = sendAndReceive(new Message(
						MessageType.WORK_INIT_REQUEST, null));

				if (message.getType() == MessageType.WORK_INIT_RESPONSE) {
					ObjectMapper mapper = new ObjectMapper();
					Object[] registration = mapper.convertValue(message.getData(), Object[].class);
					double[][] distances = mapper.convertValue(registration[0], double[][].class);
					
					GaTask gaTask = mapper.convertValue(registration[1], GaTask.class);
					
					GaResult gaResult = new GaResult();
					int counter = 0;
					while (message.getType() != MessageType.SHUTDOWN) {
						counter++;
						if (counter % logSteps == 0) {
							long endTime = System.currentTimeMillis();
							LOGGER.info("{}ms for last {} computations",
									endTime - startTime, logSteps);
							startTime = System.currentTimeMillis();
							counter = 0;
						}
						
						gaResult.setId(gaTask.getId());
						gaResult.setSlot(gaTask.getSlot());
						gaResult.setResult(GaFitness.computeFitness(distances,
								gaTask.getGenome()));
						
						message = sendAndReceive(new Message(
								MessageType.WORK_REQUEST, gaResult));
						
						gaTask = mapper.convertValue(message.getData(), GaTask.class);
					}
				}
			}
			LOGGER.info("############# {} Worker stopped ###############", RestGaWorker.class.getSimpleName());
		} catch (Exception e) {
			LOGGER.error("Worker error", e);
			return;
		}
	}

	private Message sendAndReceive(Message message) {
		Response response = client.target(url)
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(message, MediaType.APPLICATION_JSON));

		return response.readEntity(Message.class);
	}
}
