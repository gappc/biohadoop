package at.ac.uibk.dps.biohadoop.ga.worker;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageDecoder;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = MessageDecoder.class)
public class KryoGaWorker {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(KryoGaWorker.class);

	private double[][] distances;
	private long startTime = System.currentTimeMillis();
	private int counter = 0;
	private int logSteps = 1000;
	private CountDownLatch latch = new CountDownLatch(1);

	public static void main(String[] args) throws Exception {
		LOGGER.info("############# {} started ##############",
				KryoGaWorker.class.getSimpleName());

		LOGGER.info("args.length: {}", args.length);
		for (String s : args) {
			LOGGER.info(s);
		}

		String masterHostname = args[0];

		LOGGER.info("######### {} client calls master at: {}",
				KryoGaWorker.class.getSimpleName(), masterHostname);
		new KryoGaWorker(masterHostname, 30015);
	}

	public KryoGaWorker() {
	}

	public KryoGaWorker(String hostname, int port) throws DeploymentException,
			IOException, InterruptedException, EncodeException,
			ClassNotFoundException {

		Log.set(Log.LEVEL_DEBUG);

		final Client client = new Client(64 * 1024, 64 * 1024);
		client.start();
		client.connect(15000, "localhost", 30015);

		Kryo kryo = client.getKryo();
		kryo.register(Message.class);
		kryo.register(MessageType.class);
		kryo.register(GaTask.class);
		kryo.register(GaResult.class);
		kryo.register(Object[].class);
		kryo.register(double[][].class);
		kryo.register(double[].class);
		kryo.register(int[].class);
		kryo.register(StopTask.class);

		client.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if (object instanceof Message) {
					Message message = (Message) object;
					MessageType messageType = MessageType.NONE;
					GaResult response = null;

					counter++;
					if (counter % logSteps == 0) {
						long endTime = System.currentTimeMillis();
						LOGGER.info("{}ms for last {} computations",
								endTime - startTime, logSteps);
						startTime = System.currentTimeMillis();
						counter = 0;
					}

					if (message.getType() == MessageType.REGISTRATION_RESPONSE) {
						LOGGER.info("Registration successful");
						messageType = MessageType.WORK_INIT_REQUEST;
						response = null;
					}

					if (message.getType() == MessageType.WORK_INIT_RESPONSE) {
						LOGGER.debug("WORK_INIT_RESPONSE");
						Object[] data = (Object[]) message.getData();
						distances = (double[][]) data[0];
						GaTask task = (GaTask) data[1];

						messageType = MessageType.WORK_REQUEST;
						response = computeResult(task);
					}
					if (message.getType() == MessageType.WORK_RESPONSE) {
						LOGGER.debug("WORK_RESPONSE");
						GaTask task = (GaTask) message.getData();

						messageType = MessageType.WORK_REQUEST;
						response = computeResult(task);
					}
					if (message.getType() == MessageType.SHUTDOWN) {
						LOGGER.info(
								"############# {} Worker stopped ###############",
								KryoGaWorker.class.getSimpleName());
						client.close();
						latch.countDown();
					}

					connection.sendTCP(new Message(messageType, response));
				}
			}
		});
		register(client);
		latch.await();
	}

	private void register(Client client) {
		Message message = new Message(MessageType.REGISTRATION_REQUEST, null);
		client.sendTCP(message);
	}

	private GaResult computeResult(GaTask task) {
		GaResult gaResult = new GaResult();
		gaResult.setSlot(task.getSlot());
		gaResult.setResult(GaFitness.computeFitness(distances, task.getGenome()));
		gaResult.setId(task.getId());
		return gaResult;
	}
}
