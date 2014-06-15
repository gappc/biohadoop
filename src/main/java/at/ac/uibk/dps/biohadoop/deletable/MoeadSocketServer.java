package at.ac.uibk.dps.biohadoop.deletable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoeadSocketServer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MoeadSocketServer.class);
	private String className = MoeadSocketServer.class.getSimpleName();

	public MoeadSocketServer() {
		LOGGER.info("Starting {}", className);
		Thread thread = new Thread(new MoeadSocketServerRunnable(),
				MoeadSocketServerRunnable.class.getSimpleName());
		thread.start();
	}
}
