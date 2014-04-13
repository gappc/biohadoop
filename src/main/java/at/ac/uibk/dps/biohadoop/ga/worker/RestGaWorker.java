package at.ac.uibk.dps.biohadoop.ga.worker;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;

public class RestGaWorker {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RestGaWorker.class);

	public static void main(String[] args) throws InterruptedException {
		LOGGER.info("!!!!!!!GaWorker args[0]: " + args[0]);
		RestGaWorker gaWorker = new RestGaWorker();
		gaWorker.runClient(args);
	}

	private void runClient(String[] args) {
		try {
			LOGGER.info("############# GA Worker started ##############");

			LOGGER.info("args.length: " + args.length);
			for (String s : args) {
				LOGGER.info(s);
			}

			String masterHostname = args[0];

			String url = "http://" + masterHostname + ":30000/rs/ga";
			String initUrl = url + "/init";
			String workerUrl = url + "/work";

			LOGGER.info("######### client calls url: " + url);
			Client client = ClientBuilder.newClient();

			Response response = client.target(initUrl)
					.request(MediaType.APPLICATION_JSON).get();
			double[][] distances = response.readEntity(double[][].class);

			long start = System.currentTimeMillis();

			GaResult gaResult = new GaResult();
			gaResult.setSlot(-1);
			gaResult.setResult(-1);

			response = client.target(workerUrl)
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(gaResult, MediaType.APPLICATION_JSON));
			GaTask task = response.readEntity(GaTask.class);

			int counter = 0;
			do {
				gaResult.setId(task.getId());
				gaResult.setSlot(task.getSlot());
				gaResult.setResult(GaFitness.computeFitness(distances,
						task.getGenome()));

				response = client
						.target(workerUrl)
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.entity(gaResult,
								MediaType.APPLICATION_JSON));

				task = response.readEntity(GaTask.class);

				if (counter % 1e2 == 0) {
					LOGGER.info("response from ApplicationMaster: " + counter
							+ " | took " + (System.currentTimeMillis() - start)
							+ "ms");
					start = System.currentTimeMillis();
				}
				counter++;
			} while (counter < Integer.MAX_VALUE);
			LOGGER.info("############# GA Worker stopped ###############");
		} catch (Exception e) {
			LOGGER.error("Worker error", e);
			return;
		}
	}
}
