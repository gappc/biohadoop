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

import at.ac.uibk.dps.biohadoop.moead.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.moead.algorithm.MoeadResult;
import at.ac.uibk.dps.biohadoop.moead.algorithm.MoeadTask;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;

public class SocketMoeadWorker {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SocketMoeadWorker.class);

	private static String className = SocketMoeadWorker.class.getSimpleName();
	private int logSteps = 1000;

	public static void main(String[] args) throws Exception {
		LOGGER.info("############# {} started ##############", className);
		LOGGER.debug("args.length: {}", args.length);
		for (String s : args) {
			LOGGER.debug(s);
		}

		String masterHostname = args[0];

		LOGGER.info(
				"############# {} client calls master at: {} #############",
				className, masterHostname);
		new SocketMoeadWorker(masterHostname, 30001);
	}

	public SocketMoeadWorker() {
	}

	// TODO use try-with-resource
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
				LOGGER.info("{}ms for last {} computations", endTime
						- startTime, logSteps);
				startTime = System.currentTimeMillis();
				counter = 0;
				os.reset();
			}

			messageType = MessageType.NONE;
			response = null;
			Message message = (Message) is.readUnshared();

			if (message.getType() == MessageType.REGISTRATION_RESPONSE) {
				LOGGER.info("{} registration successful", className);
				messageType = MessageType.WORK_INIT_REQUEST;
				response = null;
			}

			if (message.getType() == MessageType.WORK_INIT_RESPONSE) {
				LOGGER.debug("{} WORK_INIT_RESPONSE", className);
				MoeadTask task = (MoeadTask) message.getData();
				messageType = MessageType.WORK_REQUEST;
				response = computeResult(task);
			}
			if (message.getType() == MessageType.WORK_RESPONSE) {
				LOGGER.debug("{} WORK_RESPONSE", className);
				MoeadTask task = (MoeadTask) message.getData();
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

		LOGGER.info("############# {} stopped #############", className);
	}

	private void register(ObjectOutputStream os) throws EncodeException,
			IOException {
		Message message = new Message(MessageType.REGISTRATION_REQUEST, null);
		os.writeUnshared(message);
		os.flush();
	}

	private MoeadResult computeResult(MoeadTask task) {
		double[] result = new double[2];
		result[0] = Functions.f1(task.getY());
		result[1] = Functions.f2(task.getY());
		MoeadResult moeadResult = new MoeadResult(task.getId(), task.getSlot(),
				result);
		return moeadResult;
	}
}
