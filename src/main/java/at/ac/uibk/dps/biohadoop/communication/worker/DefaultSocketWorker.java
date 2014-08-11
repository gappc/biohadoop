package at.ac.uibk.dps.biohadoop.communication.worker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

public class DefaultSocketWorker<R, T, S> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultSocketWorker.class);

	private final RemoteExecutable<R, T, S> worker;

	private static String className = ClassnameProvider
			.getClassname(DefaultSocketWorker.class);
	
	private R registrationObject;
	
	private int logSteps = 1000;

	public DefaultSocketWorker(Class<? extends RemoteExecutable<R, T, S>> workerClass)
			throws InstantiationException, IllegalAccessException {
		worker = workerClass.newInstance();
	}

	public void run(String host, int port) throws WorkerException {
		try {
			// TODO: implement timeout
			Socket clientSocket = new Socket(host, port);
			ObjectOutputStream os = new ObjectOutputStream(
					new BufferedOutputStream(clientSocket.getOutputStream()));
			os.flush();
			ObjectInputStream is = new ObjectInputStream(
					new BufferedInputStream(clientSocket.getInputStream()));

			doRegistration(os, host, port);
			handleRegistrationResponse(is, os);
			doWorkInit(os);
			handleWork(is, os);

			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException | ClassNotFoundException e) {
			throw new WorkerException("Could not communicate with " + host
					+ ":" + port, e);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			ObjectOutputStream os) throws IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Message<T> message = receive(is);
		LOG.info("{} registration successful", className);

		registrationObject = (R)message.getTask().getData();
	}

	private void doWorkInit(ObjectOutputStream os) throws IOException {
		LOG.debug("{} starting work");
		Message<Object> message = new Message<Object>(
				MessageType.WORK_INIT_REQUEST, null);
		send(os, message);
	}

	private void handleWork(ObjectInputStream is, ObjectOutputStream os)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException {
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

			Task<T> inputTask = inputMessage.getTask();

			S response = worker.compute(inputTask.getData(), registrationObject);

			Task<S> responseTask = new Task<S>(inputTask.getTaskId(), response);

			Message<S> message = new Message<S>(MessageType.WORK_REQUEST,
					responseTask);
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
