package at.ac.uibk.dps.biohadoop.performance.test.master.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceSocketServer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PerformanceSocketServer.class);
	private String className = PerformanceSocketServer.class.getSimpleName();

	public PerformanceSocketServer() {
		LOGGER.info("Starting {}", className);
		Thread thread = new Thread(new PerformanceSocketServerRunnable(),
				PerformanceSocketServerRunnable.class.getSimpleName());
		thread.start();
	}
}
