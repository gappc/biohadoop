package at.ac.uibk.dps.biohadoop.deletable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GaSocketServer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GaSocketServer.class);
	private String className = GaSocketServer.class.getSimpleName();

	public GaSocketServer() {
		LOGGER.info("Starting {}", className);
		Thread thread = new Thread(new GaSocketServerRunnable(),
				GaSocketServerRunnable.class.getSimpleName());
		thread.start();
	}
}
