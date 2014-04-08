package at.ac.uibk.dps.biohadoop.worker;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.entity.Task;

public class SimpleWorker {

	private static Logger logger = LoggerFactory.getLogger(SimpleWorker.class);

	public static void main(String[] args) throws InterruptedException {
		logger.info("!!!!!!!SimpleWorker args[0]: " + args[0]);
		SimpleWorker.runClient(args);
	}

	public static void runClient(String[] args) {
		Logger logger = LoggerFactory.getLogger("BIOHADOOP-CUSTOMWORKER");

		try {
			logger.info("############# Simple Worker started ##############");

			logger.info("args.length: " + args.length);
			for (String s : args) {
				logger.info(s);
			}

			String masterHostname = args[0];

			String url = "http://" + masterHostname + ":30000/rs/simple";
			logger.info("######### client calls url: " + url);
			Client client = ClientBuilder.newClient();

			int counter = Integer.MAX_VALUE;
			do {
				Response response = client.target(url)
						.request(MediaType.APPLICATION_JSON).get();
				try {
					Task task = response.readEntity(Task.class);
					counter = task.getId();
					logger.info("############# Simple Worker: response from ApplicationMaster: "
							+ counter);
				} catch (Exception e) {
					logger.error("Error reading Task", e);
				}

				if (counter > 0) {
					Thread.sleep(5000);
				}
			} while (counter > 0);
			logger.info("############# Simple Worker stopped ###############");
		} catch (Exception e) {
			logger.error("Worker error", e);
			return;
		}
	}

}
