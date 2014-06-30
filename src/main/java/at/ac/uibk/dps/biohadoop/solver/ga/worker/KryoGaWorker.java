package at.ac.uibk.dps.biohadoop.solver.ga.worker;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.connection.WorkerConnection;
import at.ac.uibk.dps.biohadoop.connection.kryo.KryoObjectRegistration;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.solver.ga.master.kryo.GaKryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class KryoGaWorker implements WorkerConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(KryoGaWorker.class);

	private double[][] distances;
	private long startTime = System.currentTimeMillis();
	private int counter = 0;
	private int logSteps = 1000;
	private CountDownLatch latch = new CountDownLatch(1);

	public static void main(String[] args) throws Exception {
		LOG.info("############# {} started ##############",
				KryoGaWorker.class.getSimpleName());

		LOG.info("args.length: {}", args.length);
		for (String s : args) {
			LOG.info(s);
		}

		String host = args[0];
		int port = Integer.valueOf(args[1]);

		LOG.info("######### {} client calls master at: {}:{}",
				KryoGaWorker.class.getSimpleName(), host, port);
		new KryoGaWorker(host, port);
	}
	
	public KryoGaWorker() {
	}

	@Override
	public String getWorkerParameters() {
		String prefix = new GaKryo().getPrefix();
		String hostname = Environment.getPrefixed(prefix, Environment.KRYO_SOCKET_HOST);
		String port = Environment.getPrefixed(prefix, Environment.KRYO_SOCKET_PORT);
		return hostname + " " + port;
	}
	
	public KryoGaWorker(String hostname, int port) throws DeploymentException,
			IOException, InterruptedException, EncodeException,
			ClassNotFoundException {

		Log.set(Log.LEVEL_DEBUG);

		final Client client = new Client(64 * 1024, 64 * 1024);
		client.start();
		client.connect(10000, hostname, port);

		Kryo kryo = client.getKryo();
		KryoObjectRegistration.register(kryo);

		client.addListener(new Listener() {
			public void received(Connection connection, Object object) {
				if (object instanceof Message) {
					Message<?> inputMessage = (Message<?>) object;

					counter++;
					if (counter % logSteps == 0) {
						long endTime = System.currentTimeMillis();
						LOG.info("{}ms for last {} computations",
								endTime - startTime, logSteps);
						startTime = System.currentTimeMillis();
						counter = 0;
					}

					if (inputMessage.getType() == MessageType.REGISTRATION_RESPONSE) {
						LOG.info("Registration successful");
						Double[][] inputDistances = (Double[][])inputMessage.getPayload().getData();
						convertDistances(inputDistances);
						Message<?> message = new Message<Object>(MessageType.WORK_INIT_REQUEST, null);
						connection.sendTCP(message);
					}

					if (inputMessage.getType() == MessageType.WORK_INIT_RESPONSE || inputMessage.getType() == MessageType.WORK_RESPONSE) {
						LOG.debug("WORK_INIT_RESPONSE | WORK_RESPONSE");
						
						Task<?> inputTask = inputMessage.getPayload();
						
						@SuppressWarnings({ "rawtypes", "unchecked" })
						Task<Double> response = computeResult((Task)inputTask);
						
						Message<Double> message = new Message<Double>(
								MessageType.WORK_REQUEST, response);

						connection.sendTCP(message);
					}
					if (inputMessage.getType() == MessageType.SHUTDOWN) {
						LOG.info(
								"############# {} Worker stopped ###############",
								KryoGaWorker.class.getSimpleName());
						client.close();
						latch.countDown();
					}
				}
			}
		});
		register(client);
		latch.await();
	}

	private void register(Client client) {
		Message<?> message = new Message<Object>(MessageType.REGISTRATION_REQUEST, null);
		client.sendTCP(message);
	}
	
	private void convertDistances(Double[][] inputDistances) {
		int length1 = inputDistances.length;
		int length2 = length1 != 0 ? inputDistances[0].length : 0;
		distances = new double[length1][length2];
		for (int i = 0; i < length1; i++) {
			for (int j = i; j < length2; j++) {
				distances[i][j] = inputDistances[i][j];
				distances[j][i] = inputDistances[j][i];
			}
		}
	}

	private Task<Double> computeResult(Task<int[]> task) {
		double fitness = GaFitness.computeFitness(distances, task.getData());
		Task<Double> response = new Task<Double>(task.getTaskId(), fitness);
		return response;
	}
}
