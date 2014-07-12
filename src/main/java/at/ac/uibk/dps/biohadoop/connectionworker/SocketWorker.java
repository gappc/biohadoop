package at.ac.uibk.dps.biohadoop.connectionworker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.connection.WorkerConnection;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.endpoint.WorkerEndpoint;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.PerformanceLogger;

public abstract class SocketWorker<T, S> implements WorkerEndpoint<T, S>, WorkerConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketWorker.class);

	private static String className = Helper.getClassname(SocketWorker.class);
	private int logSteps = 1000;
	
	@Override
	public String getWorkerParameters() throws Exception {
		Master masterEndpoint = getMasterEndpoint().newInstance();
		String prefix = masterEndpoint.getQueueName();
		String hostname = Environment.getPrefixed(prefix, Environment.SOCKET_HOST);
		String port = Environment.getPrefixed(prefix, Environment.SOCKET_PORT);
		return hostname + " " + port;
	}

	public void run(String host, int port) throws Exception {
		Socket clientSocket = new Socket(host, port);
		ObjectOutputStream os = new ObjectOutputStream(
				new BufferedOutputStream(clientSocket.getOutputStream()));
		os.flush();
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(
				clientSocket.getInputStream()));

		doRegistration(os, host, port);
		handleRegistrationResponse(is, os);
		doWorkInit(os);
		handleWork(is, os);

		is.close();
		os.close();
		clientSocket.close();
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
		Message<T> message = receive(is);
		LOG.info("{} registration successful", className);

		Object data = message.getPayload().getData();
		readRegistrationObject(data);
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

			Message<T> inputMessage = receive(is);

			LOG.debug("{} WORK_RESPONSE", className);

			if (inputMessage.getType() == MessageType.SHUTDOWN) {
				break;
			}

			Task<T> inputTask = inputMessage.getPayload();
			
			S response = compute((T) inputTask.getData());

			Task<S> responseTask = new Task<S>(inputTask
					.getTaskId(), response);

			Message<S> message = new Message<S>(
					MessageType.WORK_REQUEST, responseTask);
			send(os, message);
		}
	}

	@SuppressWarnings("unchecked")
	private Message<T> receive(ObjectInputStream is) throws IOException,
			ClassNotFoundException {
		return (Message<T>) is.readUnshared();
	}

	private void send(ObjectOutputStream os, Message<?> message)
			throws IOException {
		os.writeUnshared(message);
		os.flush();
	}

}