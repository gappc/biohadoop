package at.ac.uibk.dps.biohadoop.communication.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerStarter {

	private static final Logger LOG = LoggerFactory
			.getLogger(WorkerStarter.class);

	public static void main(String[] args) {
		LOG.info("############# {} starting ##############", args[0]);

		LOG.info("Program arguments: (args.length: {})", args.length);
		for (int i = 0; i < args.length; i++) {
			LOG.info("arg[{}] = {}", i, args[i]);
		}

		try {
			@SuppressWarnings("unchecked")
			Class<? extends WorkerEndpoint> workerEndpointClass = (Class<? extends WorkerEndpoint>) Class
					.forName(args[0]);
			WorkerEndpoint workerEndpoint = workerEndpointClass.newInstance();
			workerEndpoint.configure(args);
			workerEndpoint.start();
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | WorkerException e) {
			LOG.error("Error while starting Worker {}", args[0], e);
		}

		LOG.info("Worker finished");
	}

}
