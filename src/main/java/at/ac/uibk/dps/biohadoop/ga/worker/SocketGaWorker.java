package at.ac.uibk.dps.biohadoop.ga.worker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.algorithm.GaFitness;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageDecoder;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketEncoder;

@ClientEndpoint(encoders = WebSocketEncoder.class, decoders = MessageDecoder.class)
public class SocketGaWorker {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SocketGaWorker.class);

	private double[][] distances;
	private long start = System.currentTimeMillis();
	private int counter = 0;

	public static void main(String[] args) throws Exception {
		LOGGER.info("args.length: " + args.length);
		for (String s : args) {
			LOGGER.info(s);
		}

		String masterHostname = args[0];

		String url = "ws://" + masterHostname + ":30000/websocket/ga";

		LOGGER.info("######### SocketGaWorker client calls url: " + url);
		new SocketGaWorker("localhost", 30001);
	}

	public SocketGaWorker() {
	}

	public SocketGaWorker(String hostname, int port)
			throws DeploymentException, IOException, InterruptedException,
			EncodeException, ClassNotFoundException {
		Socket clientSocket = new Socket(hostname, 30001);
		ObjectOutputStream os = new ObjectOutputStream(
				new BufferedOutputStream(clientSocket.getOutputStream()));
		os.flush();
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(
				clientSocket.getInputStream()));

		register(os);

		MessageType messageType = MessageType.NONE;
		Object response = null;

		while (true) {
			counter++;
			if (counter % 1000 == 0) {
				LOGGER.info("SocketGaWorker Received Message: "
						+ (System.currentTimeMillis() - start) + "ms");
				this.start = System.currentTimeMillis();
				counter = 0;
				os.reset();
			}

			messageType = MessageType.NONE;
			response = null;
			Message message = (Message) is.readObject();

			if (message.getType() == MessageType.REGISTRATION_RESPONSE) {
				LOGGER.info("SocketGaWorker registration successful");
				messageType = MessageType.WORK_INIT_REQUEST;
				response = null;
			}

			if (message.getType() == MessageType.WORK_INIT_RESPONSE) {
				LOGGER.debug("SocketGaWorker WORK_INIT_RESPONSE");
				Object[] data = (Object[]) message.getData();
				distances = (double[][]) data[0];
				GaTask task = (GaTask)data[1];
				messageType = MessageType.WORK_REQUEST;
				response = computeResult(task);
			}
			if (message.getType() == MessageType.WORK_RESPONSE) {
				LOGGER.debug("SocketGaWorker WORK_RESPONSE");
				GaTask task = (GaTask)message.getData();
				messageType = MessageType.WORK_REQUEST;
				response = computeResult(task);
			}
			if (message.getType() == MessageType.SHUTDOWN) {
				LOGGER.info("SocketGaWorker got SHUTDOWN message, now shutting down");
				break;
			}

			os.writeObject(new Message(messageType, response));
			os.flush();
		}
		is.close();
		os.close();
		clientSocket.close();
	}

	private void register(ObjectOutputStream os) throws EncodeException,
			IOException {
		Message message = new Message(MessageType.REGISTRATION_REQUEST, null);
		os.writeObject(message);
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
