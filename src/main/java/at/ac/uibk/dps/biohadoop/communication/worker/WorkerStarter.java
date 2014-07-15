package at.ac.uibk.dps.biohadoop.communication.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerStarter {

	private static final Logger LOG = LoggerFactory
			.getLogger(WorkerStarter.class);

	private static final int MAX_ARGS_SIZE = 3;

	private WorkerStarter() {
	}
	
	public static void main(String[] args) {
		if (args.length < MAX_ARGS_SIZE) {
			String message = "Number of arguments to low, expected "
					+ args.length + ", got " + MAX_ARGS_SIZE;
			LOG.error(message);
			throw new IllegalArgumentException(message);
		}

		String className = args[0];
		
		try {
			Class<?> clazz = Class.forName(className);
			WorkerEndpoint<?, ?> workerEndpoint = (WorkerEndpoint<?, ?>) clazz
					.newInstance();

			LOG.info("############# {} started ##############",
					clazz.getSimpleName());

			LOG.info("args.length: {}", args.length);
			for (String s : args) {
				LOG.info(s);
			}
			
			String host = args[1];
			int port = Integer.parseInt(args[2]);

			LOG.info("######### {} client calls Biohadoop Master at: {}:{}",
					clazz.getSimpleName(), host, port);
			
			workerEndpoint.run(host, port);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			LOG.error("Could not run worker {}", className, e);
		} catch (WorkerException e) {
			LOG.error("Error while running worker {}", className, e);
		}
		
		LOG.info("Worker finished");
		
	}
}
