package at.ac.uibk.dps.biohadoop.communication.worker;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.kryo.KryoObjectRegistration;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.Task;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public abstract class KryoWorker2<T, S> implements WorkerEndpoint<T, S> {//,
//		WorkerParameter {

	private static final Logger LOG = LoggerFactory.getLogger(KryoWorker2.class);

	private long startTime = System.currentTimeMillis();
	private int counter = 0;
	private int logSteps = 1000;
	private CountDownLatch latch = new CountDownLatch(1);

//	@Override
//	public String getWorkerParameters() throws Exception {
//		MasterEndpoint masterEndpoint = getMasterEndpoint().newInstance();
//		String prefix = masterEndpoint.getQueueName();
//		String hostname = Environment.getPrefixed(prefix,
//				Environment.KRYO_SOCKET_HOST);
//		String port = Environment.getPrefixed(prefix,
//				Environment.KRYO_SOCKET_PORT);
//		return hostname + " " + port;
//	}

	@Override
	public void run(String host, int port) throws WorkerException {
		Log.set(Log.LEVEL_DEBUG);

		final Client client = new Client(64 * 1024, 64 * 1024);
		client.start();
		try {
			client.connect(10000, host, port);
		} catch (IOException e) {
			throw new WorkerException("Could not communicate with " + host + ":" + port,
					e);
		}

		Kryo kryo = client.getKryo();
		KryoObjectRegistration.register(kryo);

		client.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if (object instanceof Message) {
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
						readRegistrationObject(data);
						Message<?> message = new Message<Object>(
								MessageType.WORK_INIT_REQUEST, null);
						connection.sendTCP(message);
					}

					if (inputMessage.getType() == MessageType.WORK_INIT_RESPONSE
							|| inputMessage.getType() == MessageType.WORK_RESPONSE) {
						LOG.debug("WORK_INIT_RESPONSE | WORK_RESPONSE");

						Task<?> inputTask = inputMessage.getPayload();

						@SuppressWarnings("unchecked")
						S response = compute((T) inputTask.getData());

						Task<S> responseTask = new Task<S>(inputTask
								.getTaskId(), response);

						Message<S> message = new Message<S>(
								MessageType.WORK_REQUEST, responseTask);

						connection.sendTCP(message);
					}
					if (inputMessage.getType() == MessageType.SHUTDOWN) {
						LOG.info(
								"############# {} Worker stopped ###############",
								KryoWorker2.class.getSimpleName());
						client.close();
						latch.countDown();
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
