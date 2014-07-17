package at.ac.uibk.dps.biohadoop.communication.worker;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.master.kryo.KryoObjectRegistration;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.Task;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class SuperKryoWorker<T, S> implements WorkerParameter {

	private static final Logger LOG = LoggerFactory
			.getLogger(SuperKryoWorker.class);

	private final Class<? extends SuperWorker<T, S>> workerClass;

	private long startTime = System.currentTimeMillis();
	private int counter = 0;
	private int logSteps = 1000;
	private CountDownLatch latch = new CountDownLatch(1);

	public SuperKryoWorker(Class<? extends SuperWorker<T, S>> workerClass) {
		this.workerClass = workerClass;
	}

	@Override
	public String getWorkerParameters() throws Exception {
		String prefix = ((KryoWorkerAnnotation) workerClass
				.getAnnotation(KryoWorkerAnnotation.class)).master()
				.getCanonicalName();
		String hostname = Environment.getPrefixed(prefix,
				Environment.KRYO_SOCKET_HOST);
		String port = Environment.getPrefixed(prefix,
				Environment.KRYO_SOCKET_PORT);
		return hostname + " " + port;
	}

	public void run(String host, int port) throws WorkerException {
		Log.set(Log.LEVEL_DEBUG);

		final Client client = new Client(64 * 1024, 64 * 1024);
		client.start();
		try {
			client.connect(10000, host, port);
		} catch (IOException e) {
			throw new WorkerException("Could not communicate with " + host
					+ ":" + port, e);
		}

		Kryo kryo = client.getKryo();
		KryoObjectRegistration.register(kryo);

		client.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if (object instanceof Message) {
					try {
					Message<?> inputMessage = (Message<?>) object;

					counter++;
					if (counter % logSteps == 0) {
						long endTime = System.currentTimeMillis();
						LOG.info("{}ms for last {} computations", endTime
								- startTime, logSteps);
						startTime = System.currentTimeMillis();
						counter = 0;
					}

					if (inputMessage.getType() == MessageType.REGISTRATION_RESPONSE) {
						LOG.info("Registration successful");
						Object data = inputMessage.getPayload().getData();
						SuperWorker<T, S> worker = workerClass.newInstance();
						worker.readRegistrationObject(data);
						Message<?> message = new Message<Object>(
								MessageType.WORK_INIT_REQUEST, null);
						connection.sendTCP(message);
					}

					if (inputMessage.getType() == MessageType.WORK_INIT_RESPONSE
							|| inputMessage.getType() == MessageType.WORK_RESPONSE) {
						LOG.debug("WORK_INIT_RESPONSE | WORK_RESPONSE");

						Task<?> inputTask = inputMessage.getPayload();

						SuperWorker<T, S> worker = workerClass.newInstance();
						@SuppressWarnings("unchecked")
						S response = worker.compute((T) inputTask.getData());

						Task<S> responseTask = new Task<S>(inputTask
								.getTaskId(), response);

						Message<S> message = new Message<S>(
								MessageType.WORK_REQUEST, responseTask);

						connection.sendTCP(message);
					}
					if (inputMessage.getType() == MessageType.SHUTDOWN) {
						LOG.info(
								"############# {} Worker stopped ###############",
								SuperKryoWorker.class.getSimpleName());
						client.close();
						latch.countDown();
					}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		register(client);
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new WorkerException(
					"Error while waiting for worker to finish", e);
		}
	}

	private void register(Client client) {
		Message<?> message = new Message<Object>(
				MessageType.REGISTRATION_REQUEST, null);
		client.sendTCP(message);
	}

}
