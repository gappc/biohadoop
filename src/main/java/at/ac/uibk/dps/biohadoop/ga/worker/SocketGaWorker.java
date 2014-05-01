package at.ac.uibk.dps.biohadoop.ga.worker;

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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class SocketGaWorker {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SocketGaWorker.class);

	private double[][] distances;
	private int logSteps = 1000;

	public static void main(String[] args) throws Exception {
		LOGGER.info("############# {} started ##############",
				SocketGaWorker.class.getSimpleName());
		LOGGER.debug("args.length: {}", args.length);
		for (String s : args) {
			LOGGER.debug(s);
		}

		String masterHostname = args[0];

		LOGGER.info("############# {} client calls master at: {} #############",
				SocketGaWorker.class.getSimpleName(), masterHostname);
		new SocketGaWorker(masterHostname, 30001);
	}

	public SocketGaWorker() {
	}

	public SocketGaWorker(String hostname, int port)
			throws DeploymentException, IOException, InterruptedException,
			EncodeException, ClassNotFoundException {
		Socket clientSocket = new Socket(hostname, port);
		ObjectOutputStream os = new ObjectOutputStream(
				new BufferedOutputStream(clientSocket.getOutputStream()));
		os.flush();
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(
				clientSocket.getInputStream()));

		// Kryo kryo = new Kryo();
		// kryo.setReferences(false);
		// Output output = new Output(clientSocket.getOutputStream());
		// Input input = new Input(clientSocket.getInputStream());

		register(os);
		// registerKryo(kryo, output);

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
			// Message message = kryo.readObject(input, Message.class);

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

			// kryo.writeObject(output, new Message(messageType, response));
			// output.flush();
			os.writeUnshared(new Message(messageType, response));
			os.flush();
		}
		is.close();
		os.close();
		clientSocket.close();

		LOGGER.info("############# {} stopped #############",
				SocketGaWorker.class.getSimpleName());
	}

	private void register(ObjectOutputStream os) throws EncodeException,
			IOException {
		Message message = new Message(MessageType.REGISTRATION_REQUEST, null);
		os.writeUnshared(message);
		os.flush();
	}

	private void registerKryo(Kryo kryo, Output output) throws EncodeException,
			IOException {
		Message message = new Message(MessageType.REGISTRATION_REQUEST, null);
		kryo.writeObject(output, message);
		output.flush();
	}

	private GaResult computeResult(GaTask task) {
		GaResult gaResult = new GaResult();
		gaResult.setSlot(task.getSlot());
		gaResult.setResult(GaFitness.computeFitness(distances, task.getGenome()));
		gaResult.setId(task.getId());
		return gaResult;
	}
}
