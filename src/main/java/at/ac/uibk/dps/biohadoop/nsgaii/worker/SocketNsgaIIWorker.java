package at.ac.uibk.dps.biohadoop.nsgaii.worker;

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

import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.MessageType;
import at.ac.uibk.dps.biohadoop.nsgaii.algorithm.Functions;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;

public class SocketNsgaIIWorker {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketNsgaIIWorker.class);

	private static String className = Helper.getClassname(SocketNsgaIIWorker.class);
	private int logSteps = 1000;

	public static void main(String[] args) throws Exception {
		LOG.info("############# {} started ##############", className);
		LOG.debug("args.length: {}", args.length);
		for (String s : args) {
			LOG.debug(s);
		}

		String masterHostname = args[0];

		LOG.info(
				"############# {} client calls master at: {} #############",
				className, masterHostname);
		new SocketNsgaIIWorker(masterHostname, 30001);
	}

	// TODO use try-with-resource
	public SocketNsgaIIWorker(String hostname, int port)
			throws DeploymentException, IOException, InterruptedException,
			EncodeException, ClassNotFoundException {
		Socket clientSocket = new Socket(hostname, port);
		ObjectOutputStream os = new ObjectOutputStream(
				new BufferedOutputStream(clientSocket.getOutputStream()));
		os.flush();
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(
				clientSocket.getInputStream()));

		doRegistration(os, hostname, port);
		handleRegistrationResponse(is, os);
		doWorkInit(os);
		handleWork(is, os);
		
//		register(os);
//
//		MessageType messageType = MessageType.NONE;
//		Object response = null;
//
//		long startTime = System.currentTimeMillis();
//		int counter = 0;
//		while (true) {
//			counter++;
//			if (counter % logSteps == 0) {
//				long endTime = System.currentTimeMillis();
//				LOG.info("{}ms for last {} computations", endTime
//						- startTime, logSteps);
//				startTime = System.currentTimeMillis();
//				counter = 0;
//				os.reset();
//			}
//
//			messageType = MessageType.NONE;
//			response = null;
//			Message message = (Message) is.readUnshared();
//
//			if (message.getType() == MessageType.REGISTRATION_RESPONSE) {
//				LOG.info("{} registration successful", className);
//				messageType = MessageType.WORK_INIT_REQUEST;
//				response = null;
//			}
//
//			if (message.getType() == MessageType.WORK_INIT_RESPONSE) {
//				LOG.debug("{} WORK_INIT_RESPONSE", className);
//				NsgaIITask task = (NsgaIITask) message.getData();
//				messageType = MessageType.WORK_REQUEST;
//				response = computeResult(task);
//			}
//			if (message.getType() == MessageType.WORK_RESPONSE) {
//				LOG.debug("{} WORK_RESPONSE", className);
//				NsgaIITask task = (NsgaIITask) message.getData();
//				messageType = MessageType.WORK_REQUEST;
//				response = computeResult(task);
//			}
//			if (message.getType() == MessageType.SHUTDOWN) {
//				break;
//			}
//
//			os.writeUnshared(new Message(messageType, response));
//			os.flush();
//		}
		is.close();
		os.close();
		clientSocket.close();

		LOG.info("############# {} stopped #############", className);
	}

	private void doRegistration(ObjectOutputStream os, String hostname, int port)
			throws IOException {
		LOG.debug("{} starting registration to {}:{}", className, hostname,
				port);
		Message<Object> message = new Message<Object>(
				MessageType.REGISTRATION_REQUEST, null);
		send(os, message);
	}

	private void handleRegistrationResponse(ObjectInputStream is,
			ObjectOutputStream os) throws ClassNotFoundException, IOException {
		receive(is);
		LOG.info("{} registration successful", className);
	}

	private void doWorkInit(ObjectOutputStream os) throws IOException {
		LOG.debug("{} starting work");
		Message<Object> message = new Message<Object>(
				MessageType.WORK_INIT_REQUEST, null);
		send(os, message);
	}

	private void handleWork(ObjectInputStream is, ObjectOutputStream os)
			throws IOException, ClassNotFoundException {
		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		int counter = 0;
		while (true) {
			performanceLogger.step(LOG);
			counter++;
			if (counter % 1000 == 0) {
				os.reset();
				counter = 0;
			}

			Message<double[]> inputMessage = receive(is);

			LOG.debug("{} WORK_RESPONSE", className);

			if (inputMessage.getType() == MessageType.SHUTDOWN) {
				break;
			}

			Task<double[]> inputTask = inputMessage.getPayload();
			Task<double[]> response = computeResult(inputTask);

			Message<double[]> message = new Message<double[]>(
					MessageType.WORK_REQUEST, response);
			send(os, message);
		}
	}

	private Task<double[]> computeResult(Task<double[]> task) {
		double[] result = new double[2];
		result[0] = Functions.f1(task.getData());
		result[1] = Functions.f2(task.getData());
		
		Task<double[]> response = new Task<double[]>(task.getTaskId(), result);
		return response;
	}
	
	@SuppressWarnings("unchecked")
	private <T> Message<T> receive(ObjectInputStream is) throws IOException,
			ClassNotFoundException {
		return (Message<T>) is.readUnshared();
	}

	private void send(ObjectOutputStream os, Message<?> message)
			throws IOException {
		os.writeUnshared(message);
		os.flush();
	}
}
