package at.ac.uibk.dps.biohadoop.deletable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsgaIISocketServer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NsgaIISocketServer.class);
	private String className = NsgaIISocketServer.class.getSimpleName();

	public NsgaIISocketServer() {
		LOGGER.info("Starting {}", className);
		Thread thread = new Thread(new NsgaIISocketServerRunnable(),
				NsgaIISocketServerRunnable.class.getSimpleName());
		thread.start();
	}
}
