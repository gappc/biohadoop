package at.ac.uibk.dps.biohadoop;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class SimpleWorker {

	private static Logger logger = LoggerFactory.getLogger(SimpleWorker.class);
	
	public static void main(String[] args) throws InterruptedException {
		logger.info("############# Simple Worker started ###############");
		Client client = Client.create();
		WebResource resource = client.resource("http://master:30000/simple");

		for (int i = 0; i < 10; i++) {
			String response = resource.accept(
			        MediaType.APPLICATION_JSON_TYPE,
			        MediaType.APPLICATION_XML_TYPE).
			        get(String.class);
			logger.info("############# Simple Worker: response from ApplicationMaster: " + response);
			Thread.sleep(1000);
		}
		logger.info("############# Simple Worker stopped ###############");
	}
}
