package at.ac.uibk.dps.biohadoop.worker;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.GaResult;
import at.ac.uibk.dps.biohadoop.ga.GaTask;

public class GaWorker {

	private static Logger logger = LoggerFactory.getLogger(GaWorker.class);

	public static void main(String[] args) throws InterruptedException {
		logger.info("!!!!!!!GaWorker args[0]: " + args[0]);
		GaWorker.runClient(args);
	}

	public static void runClient(String[] args) {
		Logger logger = LoggerFactory.getLogger("BIOHADOOP-CUSTOMWORKER");

		try {
			logger.info("############# GA Worker started ##############");

			logger.info("args.length: " + args.length);
			for (String s : args) {
				logger.info(s);
			}

			String masterHostname = args[0];

			String url = "http://" + masterHostname + ":30000/ga";
			String initUrl = url + "/init";
			String workerUrl = url + "/work";
			
			logger.info("######### client calls url: " + url);
			Client client = ClientBuilder.newClient();

			Response response = client.target(initUrl)
					.request(MediaType.APPLICATION_JSON).get();
			double[][] distances = response.readEntity(double[][].class);
			
			long start = System.currentTimeMillis();
			long startRound = System.currentTimeMillis();
			
			int counter = 0;
			do {
				try {
					startRound = System.currentTimeMillis();
					
					response = client.target(workerUrl)
							.request(MediaType.APPLICATION_JSON).get();
					GaTask task = response.readEntity(GaTask.class);

					GaResult gaResult = new GaResult();
					gaResult.setSlot(task.getSlot());
					gaResult.setResult(fitness(distances, task.getGenome()));

					response = client
							.target(url)
							.request(MediaType.APPLICATION_JSON)
							.post(Entity.entity(gaResult,
									MediaType.APPLICATION_JSON));

					System.out.println("Took: " + (System.currentTimeMillis() - startRound));
					
					if (counter % 1e3 == 0) {
						logger.info("response from ApplicationMaster: "
								+ counter + " | took " + (System.currentTimeMillis() - start) + "ms");
						start = System.currentTimeMillis();
					}
				} catch (Exception e) {
					logger.error("Error reading Task", e);
				}
				counter++;
//				Thread.sleep(100);
			} while (counter < Integer.MAX_VALUE);
			logger.info("############# GA Worker stopped ###############");
		} catch (Exception e) {
			logger.error("Worker error", e);
			return;
		}
	}

	private static double fitness(double[][] distances, int[] ds) {
		double pathLength = 0.0;
		for (int i = 0; i < ds.length - 1; i++) {
			pathLength += distances[ds[i]][ds[i + 1]];
		}

		pathLength += distances[ds[ds.length - 1]][ds[0]];

		return pathLength;
	}

}
