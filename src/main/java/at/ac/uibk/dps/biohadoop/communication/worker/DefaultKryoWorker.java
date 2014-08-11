package at.ac.uibk.dps.biohadoop.communication.worker;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.master.kryo.KryoObjectRegistration;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class DefaultKryoWorker<R, T, S> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultKryoWorker.class);

	private final RemoteExecutable<R, T, S> worker;
	private R registrationObject;
	
	private long startTime = System.currentTimeMillis();
	private int counter = 0;
	private int logSteps = 1000;
	private CountDownLatch latch = new CountDownLatch(1);
	
	public DefaultKryoWorker(Class<? extends RemoteExecutable<R, T, S>> workerClass)
			throws InstantiationException, IllegalAccessException {
		worker = workerClass.newInstance();
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
					Message<T> inputMessage = (Message<T>) object;

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
						registrationObject = (R)inputMessage.getTask().getData();
						Message<?> message = new Message<Object>(
								MessageType.WORK_INIT_REQUEST, null);
						connection.sendTCP(message);
					}

					if (inputMessage.getType() == MessageType.WORK_INIT_RESPONSE
							|| inputMessage.getType() == MessageType.WORK_RESPONSE) {
						LOG.debug("WORK_INIT_RESPONSE | WORK_RESPONSE");

						Task<T> inputTask = inputMessage.getTask();

						@SuppressWarnings("unchecked")
						S response = worker.compute(inputTask.getData(), registrationObject);

						Task<S> responseTask = new Task<S>(inputTask
								.getTaskId(), response);

						Message<S> message = new Message<S>(
								MessageType.WORK_REQUEST, responseTask);

						connection.sendTCP(message);
					}
					if (inputMessage.getType() == MessageType.SHUTDOWN) {
						LOG.info(
								"############# {} Worker stopped ###############",
								DefaultKryoWorker.class.getSimpleName());
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
