package at.ac.uibk.dps.biohadoop.communication.worker;

import java.lang.annotation.Annotation;

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
			LOG.info("############# {} started ##############", className);

			LOG.info("args.length: {}", args.length);
			for (String s : args) {
				LOG.info(s);
			}

			String host = args[1];
			int port = Integer.parseInt(args[2]);

			LOG.info("######### {} client calls Biohadoop Master at: {}:{}",
					className, host, port);

			Class<?> clazz = Class.forName(className);
			Class<? extends SuperWorker<Object, Object>> workerClass = (Class<? extends SuperWorker<Object, Object>>) clazz;

			// TODO make parallel
			// TODO exceptions cought in methods, should be caught here?
			buildKryoWorker(workerClass, host, port);
			buildRestWorker(workerClass, host, port);
			buildSocketWorker(workerClass, host, port);
			buildWebSocketWorker(workerClass, host, port);

			//
			// workerEndpoint.run(host, port);
			// } catch (ClassNotFoundException | InstantiationException
			// | IllegalAccessException e) {
			// LOG.error("Could not run worker {}", className, e);
		} catch (ClassNotFoundException e) {
			LOG.error("Could not run worker {}", className, e);
			// } catch (WorkerException e) {
			// LOG.error("Error while running worker {}", className, e);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOG.info("Worker finished");
	}

	private static void buildKryoWorker(
			Class<? extends SuperWorker<Object, Object>> workerClass,
			String host, int port) throws InstantiationException,
			IllegalAccessException {
		Annotation kryoWorkerAnnotation = workerClass
				.getAnnotation(KryoWorker.class);
		if (kryoWorkerAnnotation != null) {
			SuperKryoWorker<?, ?> superKryoWorker = new SuperKryoWorker<Object, Object>(
					workerClass);
			try {
				superKryoWorker.run(host, port);
			} catch (WorkerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void buildRestWorker(
			Class<? extends SuperWorker<Object, Object>> workerClass,
			String host, int port) throws InstantiationException, IllegalAccessException {
		Annotation restWorkerAnnotation = workerClass
				.getAnnotation(RestWorker.class);
		if (restWorkerAnnotation != null) {
			SuperRestWorker<?, ?> superRestWorker = new SuperRestWorker<Object, Object>(
					workerClass);
			try {
				superRestWorker.run(host, port);
			} catch (WorkerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void buildSocketWorker(
			Class<? extends SuperWorker<Object, Object>> workerClass,
			String host, int port) throws InstantiationException,
			IllegalAccessException {
		Annotation socketWorkerAnnotation = workerClass
				.getAnnotation(SocketWorker.class);
		if (socketWorkerAnnotation != null) {
			SuperSocketWorker<?, ?> superSocketWorker = new SuperSocketWorker<Object, Object>(
					workerClass);
			try {
				superSocketWorker.run(host, port);
			} catch (WorkerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void buildWebSocketWorker(
			Class<? extends SuperWorker<Object, Object>> workerClass,
			String host, int port) throws InstantiationException, IllegalAccessException {
		Annotation webSocketWorkerAnnotation = workerClass
				.getAnnotation(WebSocketWorker.class);
		if (webSocketWorkerAnnotation != null) {
			SuperWebSocketWorker<?, ?> superWebSocketWorker = new SuperWebSocketWorker<Object, Object>(
					workerClass);
			try {
				superWebSocketWorker.run(host, port);
			} catch (WorkerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
