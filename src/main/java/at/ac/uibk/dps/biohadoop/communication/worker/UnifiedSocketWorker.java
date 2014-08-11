package at.ac.uibk.dps.biohadoop.communication.worker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskId;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrapper;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.WorkerData;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;
import at.ac.uibk.dps.biohadoop.utils.convert.ConversionException;

public class UnifiedSocketWorker<R, T, S> implements WorkerEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(UnifiedSocketWorker.class);
	private static String className = ClassnameProvider
			.getClassname(UnifiedSocketWorker.class);

	private final Map<String, WorkerData<R, T, S>> workerData = new ConcurrentHashMap<>();
	private WorkerParameters parameters;
	private int logSteps = 1000;

	@Override
	public void configure(String[] args) throws WorkerException {
		parameters = WorkerParameters.getParameters(args);
	}

	@Override
	public void start() throws WorkerException {
		try {
			// TODO: implement timeout
			Socket clientSocket = new Socket(parameters.getHost(),
					parameters.getPort());
			ObjectOutputStream os = new ObjectOutputStream(
					new BufferedOutputStream(clientSocket.getOutputStream()));
			os.flush();
			ObjectInputStream is = new ObjectInputStream(
					new BufferedInputStream(clientSocket.getInputStream()));

			// doWorkInit(os);
			// doRegistration(os, host, port);
			// handleRegistrationResponse(is, os);
			handleWork(is, os);

			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {
			throw new WorkerException("Could not communicate with "
					+ parameters.getHost() + ":" + parameters.getPort(), e);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | ConversionException e) {
			throw new WorkerException(e);
		}
	}

	// private void doRegistration(ObjectOutputStream os, String hostname, int
	// port)
	// throws IOException {
	// LOG.debug("{} starting registration to {}:{}", className, hostname,
	// port);
	// Message<Object> message = new Message<Object>(
	// MessageType.REGISTRATION_REQUEST, null);
	// send(os, message);
	// }

	// private void handleRegistrationResponse(ObjectInputStream is,
	// ObjectOutputStream os) throws IOException, ClassNotFoundException,
	// InstantiationException, IllegalAccessException {
	// Message<T> message = receive(is);
	// LOG.info("{} registration successful", className);
	//
	// Object data = message.getPayload().getData();
	// worker.readRegistrationObject(data);
	// }

	// private void doWorkInit(ObjectOutputStream os) throws IOException {
	// LOG.debug("{} starting work");
	// Message<Object> message = new Message<Object>(
	// MessageType.WORK_INIT_REQUEST, null);
	// send(os, message);
	// }

	private void handleWork(ObjectInputStream is, ObjectOutputStream os)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, ConversionException {
		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		int counter = 0;

		Message<Object> requestMessage = new Message<Object>(
				MessageType.WORK_INIT_REQUEST, null);
		send(os, requestMessage);

		Message<ClassNameWrapper<T>> inputMessage = receive(is);

		while (inputMessage.getType() != MessageType.SHUTDOWN) {
			performanceLogger.step(LOG);
			counter++;
			if (counter % 1000 == 0) {
				os.reset();
				counter = 0;
			}

			LOG.debug("{} WORK_RESPONSE", className);

			Task<ClassNameWrapper<T>> task = inputMessage.getTask();
			String classString = task.getData().getClassName();

			WorkerData<R, T, S> workerEntry = getWorkerData(classString, os, is);

			RemoteExecutable<R, T, S> remoteExecutable = workerEntry
					.getRemoteExecutable();
			R initalData = workerEntry.getInitialData();
			T data = task.getData().getWrapped();

			S result = remoteExecutable.compute(data, initalData);

			Message<ClassNameWrapper<S>> outputMessage = createMessage(
					task.getTaskId(), classString, result);

			// // //////////
			// String classString = inputMessage.getTask().getData()
			// .getClassName();
			//
			// WorkerData<R, T, S> workerData = workerData.get(className);
			// if (workerData == null) {
			// Class<? extends RemoteExecutable<?, ?, ?>> className = (Class<?
			// extends RemoteExecutable<?, ?, ?>>) Class
			// .forName(classString);
			// inputMessage = new
			// Message<Object>(MessageType.REGISTRATION_REQUEST,
			// new Task(null, className.getCanonicalName()));
			// send(os, inputMessage);
			//
			// Message<ClassNameWrapper<T>> registrationMessage = receive(is);
			// Object registrationObject = registrationMessage.getTask()
			// .getData().getWrapped();
			// RemoteExecutable<?, ?, ?> unifiedCommunication = className
			// .newInstance();
			//
			// workerData = new WorkerData(unifiedCommunication,
			// registrationObject);
			// workerData.put(className, workerData);
			// }
			// RemoteExecutable<R, T, S> unifiedCommunication =
			// (RemoteExecutable<R, T, S>) workerData
			// .getRemoteExecutable();
			// Object registrationObject = workerData.getInitialData();
			//
			// // Task<T> inputTask = inputMessage.getPayload();
			//
			// T data = inputMessage.getTask().getData().getWrapped();
			// S response = ((RemoteExecutable<R, T, S>) workerData
			// .getRemoteExecutable()).compute(data,
			// (R)registrationObject);
			//
			// Task<S> responseTask = new
			// Task<S>(inputMessage.getTask().getTaskId(), result);
			//
			// Message<S> outputMessage = new
			// Message<S>(MessageType.WORK_REQUEST,
			// responseTask);
			send(os, outputMessage);

			inputMessage = receive(is);
		}
	}

	@SuppressWarnings("unchecked")
	private Message<ClassNameWrapper<T>> receive(ObjectInputStream is)
			throws IOException, ClassNotFoundException {
		return (Message<ClassNameWrapper<T>>) is.readUnshared();
	}

	private void send(ObjectOutputStream os, Message<?> message)
			throws IOException {
		os.writeUnshared(message);
		os.flush();
	}

	private WorkerData<R, T, S> getWorkerData(String classString,
			ObjectOutputStream os, ObjectInputStream is)
			throws ClassNotFoundException, IOException, ConversionException,
			InstantiationException, IllegalAccessException {
		WorkerData<R, T, S> workerEntry = workerData.get(classString);
		if (workerEntry == null) {
			ClassNameWrapper<String> wrapper = new ClassNameWrapper<>(
					classString, classString);
			Task<ClassNameWrapper<?>> responseTask = new Task<ClassNameWrapper<?>>(
					null, wrapper);
			Message<?> registrationRequest = new Message<>(
					MessageType.REGISTRATION_REQUEST, responseTask);

			send(os, registrationRequest);
			Message<ClassNameWrapper<T>> inputMessage = receive(is);

			Class<? extends RemoteExecutable<R, T, S>> className = (Class<? extends RemoteExecutable<R, T, S>>) Class
					.forName(classString);
			RemoteExecutable<R, T, S> remoteExecutable = className
					.newInstance();

			T initialData = inputMessage.getTask().getData().getWrapped();
			workerEntry = new WorkerData<>(remoteExecutable, (R) initialData);

			workerData.put(classString, workerEntry);
		}
		return workerEntry;
	}

	public Message<ClassNameWrapper<S>> createMessage(TaskId taskId,
			String classString, S data) {
		ClassNameWrapper<S> wrapper = new ClassNameWrapper<>(classString, data);
		Task<ClassNameWrapper<S>> responseTask = new Task<>(taskId, wrapper);
		return new Message<>(MessageType.WORK_REQUEST, responseTask);
	}

}
