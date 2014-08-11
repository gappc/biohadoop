package at.ac.uibk.dps.biohadoop.communication.worker;

import java.lang.annotation.Annotation;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.DedicatedKryo;
import at.ac.uibk.dps.biohadoop.communication.master.DedicatedRest;
import at.ac.uibk.dps.biohadoop.communication.master.DedicatedSocket;
import at.ac.uibk.dps.biohadoop.communication.master.DedicatedWebSocket;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

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
			Class<? extends RemoteExecutable<?, ?, ?>> workerClass = (Class<? extends RemoteExecutable<?, ?, ?>>) clazz;

			// TODO make parallel
			// TODO exceptions cought in methods, should be caught here?
//			buildKryoWorker(workerClass, host, port);
//			buildRestWorker(workerClass, host, port);
//			buildSocketWorker(workerClass, host, port);
//			buildWebSocketWorker(workerClass, host, port);

			// buildUnifiedRestWorker();
//			buildUnifiedWebSocketWorker();
//			 buildUnifiedSocketWorker();
			 buildUnifiedKryoWorker();
			//
			// workerEndpoint.run(host, port);
			// } catch (ClassNotFoundException | InstantiationException
			// | IllegalAccessException e) {
			// LOG.error("Could not run worker {}", className, e);
		} catch (ClassNotFoundException e) {
			LOG.error("Could not run worker {}", className, e);
			// } catch (WorkerException e) {
			// LOG.error("Error while running worker {}", className, e);
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}

		LOG.info("Worker finished");
	}

	private static void buildKryoWorker(
			Class<? extends RemoteExecutable<?, ?, ?>> workerClass,
			String host, int port) throws InstantiationException,
			IllegalAccessException {
		Annotation kryoWorkerAnnotation = workerClass
				.getAnnotation(DedicatedKryo.class);
		if (kryoWorkerAnnotation != null) {
			DefaultKryoWorker<?, ?, ?> superKryoWorker = new DefaultKryoWorker(
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
			Class<? extends RemoteExecutable<?, ?, ?>> workerClass,
			String host, int port) throws InstantiationException,
			IllegalAccessException {
		Annotation restWorkerAnnotation = workerClass
				.getAnnotation(DedicatedRest.class);
		if (restWorkerAnnotation != null) {
			DefaultRestWorker<?, ?, ?> superRestWorker = new DefaultRestWorker(
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
			Class<? extends RemoteExecutable<?, ?, ?>> workerClass,
			String host, int port) throws InstantiationException,
			IllegalAccessException {
		Annotation socketWorkerAnnotation = workerClass
				.getAnnotation(DedicatedSocket.class);
		if (socketWorkerAnnotation != null) {
			DefaultSocketWorker<?, ?, ?> superSocketWorker = new DefaultSocketWorker(
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
			Class<? extends RemoteExecutable<?, ?, ?>> workerClass,
			String host, int port) throws InstantiationException,
			IllegalAccessException {
		Annotation webSocketWorkerAnnotation = workerClass
				.getAnnotation(DedicatedWebSocket.class);
		if (webSocketWorkerAnnotation != null) {
			DefaultWebSocketWorker<?, ?, ?> superWebSocketWorker = new DefaultWebSocketWorker(
					workerClass);
			try {
				superWebSocketWorker.run(host, port);
			} catch (WorkerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void buildUnifiedRestWorker() {
		LOG.info("Starting unified Rest worker");
		try {
			UnifiedRestWorker<?, ?, ?> restWorker = new UnifiedRestWorker<>(
					"at.ac.uibk.dps.biohadoop.algorithms.echo.communication.StringCommunication");
			// UnifiedRestWorker<?, ?, ?> restWorker = new
			// UnifiedRestWorker<>("");
			restWorker.run("kleintroppl", 30000);
		} catch (WorkerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void buildUnifiedWebSocketWorker() {
		LOG.info("Starting unified WebSocket worker");
		try {
			UnifiedWebSocketWorker webSocketWorker = new UnifiedWebSocketWorker(
					"at.ac.uibk.dps.biohadoop.algorithms.echo.communication.StringCommunication");
//			UnifiedWebSocketWorker webSocketWorker = new UnifiedWebSocketWorker(
//					"");
			webSocketWorker.run("kleintroppl", 30000);
		} catch (WorkerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void buildUnifiedSocketWorker() {
		LOG.info("Starting unified Socket worker");
		try {
//			UnifiedSocketWorker socketWorker = new UnifiedSocketWorker("at.ac.uibk.dps.biohadoop.algorithms.echo.communication.StringCommunication");
			UnifiedSocketWorker socketWorker = new UnifiedSocketWorker("");
			socketWorker.run("kleintroppl", 30001);
		} catch (WorkerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void buildUnifiedKryoWorker() {
		LOG.info("Starting unified Kryo worker");

		final CountDownLatch latch = new CountDownLatch(1);

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
				try {
//					UnifiedKryoWorker kryoWorker = new UnifiedKryoWorker("at.ac.uibk.dps.biohadoop.algorithms.echo.communication.StringCommunication");
					UnifiedKryoWorker kryoWorker = new UnifiedKryoWorker("");
					kryoWorker.run("kleintroppl", 30001);
				} catch (WorkerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				latch.countDown();
//			}
//		}).start();

		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// UnifiedKryoWorker kryoWorker = new UnifiedKryoWorker();
		// try {
		// kryoWorker.run("kleintroppl", 30002);
		// } catch (WorkerException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// latch.countDown();
		// }
		// }).start();
//		try {
//			latch.await();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
