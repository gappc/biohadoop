package at.ac.uibk.dps.biohadoop.moead.worker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;

public class SocketMoeadWorker {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SocketMoeadWorker.class);

	private double[][] distances;
	private int logSteps = 1000;

	public static void main(String[] args) throws Exception {
		LOGGER.info("############# {} started ##############",
				SocketMoeadWorker.class.getSimpleName());
		LOGGER.debug("args.length: {}", args.length);
		for (String s : args) {
			LOGGER.debug(s);
		}

		String masterHostname = args[0];

		LOGGER.info("############# {} client calls master at: {} #############",
				SocketMoeadWorker.class.getSimpleName(), masterHostname);
		new SocketMoeadWorker(masterHostname, 30001);
	}

	public SocketMoeadWorker() {
	}
	
//	TODO use try-with-resource
	public SocketMoeadWorker(String hostname, int port)
			throws DeploymentException, IOException, InterruptedException,
			EncodeException, ClassNotFoundException {
		Socket clientSocket = new Socket(hostname, port);
		ObjectOutputStream os = new ObjectOutputStream(
				new BufferedOutputStream(clientSocket.getOutputStream()));
		os.flush();
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(
				clientSocket.getInputStream()));

		register(os);

		MessageType messageType = MessageType.NONE;
		Object response = null;

		long startTime = System.currentTimeMillis();
		int counter = 0;
		while (true) {
			counter++;
			if (counter % logSteps == 0) {
				long endTime = System.currentTimeMillis();
				LOGGER.info("{}ms for last {} computations",
						endTime - startTime, logSteps);
				startTime = System.currentTimeMillis();
				counter = 0;
				os.reset();
			}

			messageType = MessageType.NONE;
			response = null;
			Message message = (Message) is.readUnshared();

			if (message.getType() == MessageType.REGISTRATION_RESPONSE) {
				LOGGER.info("SocketGaWorker registration successful");
				messageType = MessageType.WORK_INIT_REQUEST;
				response = null;
			}

			if (message.getType() == MessageType.WORK_INIT_RESPONSE) {
				LOGGER.debug("SocketGaWorker WORK_INIT_RESPONSE");
				Object[] data = (Object[]) message.getData();
				distances = (double[][]) data[0];
				GaTask task = (GaTask) data[1];
				messageType = MessageType.WORK_REQUEST;
				response = computeResult(task);
			}
			if (message.getType() == MessageType.WORK_RESPONSE) {
				LOGGER.debug("SocketGaWorker WORK_RESPONSE");
				GaTask task = (GaTask) message.getData();
				messageType = MessageType.WORK_REQUEST;
				response = computeResult(task);
			}
			if (message.getType() == MessageType.SHUTDOWN) {
				break;
			}

			os.writeUnshared(new Message(messageType, response));
			os.flush();
		}
		is.close();
		os.close();
		clientSocket.close();

		LOGGER.info("############# {} stopped #############",
				SocketMoeadWorker.class.getSimpleName());
	}

	private void register(ObjectOutputStream os) throws EncodeException,
			IOException {
		Message message = new Message(MessageType.REGISTRATION_REQUEST, null);
		os.writeUnshared(message);
		os.flush();
	}

	private GaResult computeResult(GaTask task) {
		GaResult gaResult = new GaResult();
		gaResult.setSlot(task.getSlot());
		gaResult.setResult(GaFitness.computeFitness(distances, task.getGenome()));
		gaResult.setId(task.getId());
		return gaResult;
	}
}
