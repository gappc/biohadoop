package at.ac.uibk.dps.biohadoop.communication.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.queue.SimpleTask;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskId;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

public class DefaultMasterImpl<R, T, S> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultMasterImpl.class);

	private final TaskEndpoint<T, S> taskEndpoint;
	private Task<?> currentTask = null;

	public DefaultMasterImpl(String queueName) {
		taskEndpoint = new TaskEndpointImpl<>(queueName);
	}

	public Message<T> handleMessage(Message<S> inputMessage)
			throws HandleMessageException {
		switch (inputMessage.getType()) {
		case NONE:
			break;
		case REGISTRATION_REQUEST:
			return (Message<T>)getInitialData(inputMessage);
		case WORK_INIT_REQUEST:
			return getWorkResponse();
		case WORK_REQUEST:
			storeResult(inputMessage);
			return getWorkResponse();
		default:
			return new Message<>(MessageType.ERROR, null);
		}
		String errMsg = "Could not handle request with message " + inputMessage;
		LOG.error(errMsg);
		throw new HandleMessageException(errMsg);
	}

	private Message<R> getInitialData(Message<S> inputMessage)
			throws HandleMessageException {
		LOG.info("Got registration request");
		currentTask = null;
		try {
			String className = ((ClassNameWrappedTask<T>) inputMessage
					.getTask()).getClassName();
			Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass = (Class<? extends RemoteExecutable<R, T, S>>) Class
					.forName(className);

			R initialData = readInitialData(remoteExecutableClass);
			Task<R> task = new ClassNameWrappedTask<>(TaskId.newInstance(),
					initialData, remoteExecutableClass.getCanonicalName());
			return new Message<>(MessageType.REGISTRATION_RESPONSE, task);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			throw new HandleMessageException("Could not get initial data", e);
		}

	}

	private void storeResult(Message<S> inputMessage)
			throws HandleMessageException {
		Task<S> task = inputMessage.getTask();
		try {
			taskEndpoint.storeResult(task.getTaskId(), task.getData());
		} catch (InterruptedException e) {
			throw new HandleMessageException(
					"Error while storing result for message {}" + inputMessage);
		}
	}

	private Message<T> getWorkResponse() {
		try {
			Task<T> task = taskEndpoint.getTask();
			return new Message<>(MessageType.WORK_INIT_RESPONSE, task);
		} catch (InterruptedException e) {
			LOG.debug("Got InterruptedException, stopping work");
			currentTask = null;
			return new Message<>(MessageType.SHUTDOWN, null);
		}
	}

	private R readInitialData(
			Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		RemoteExecutable<R, T, S> master = remoteExecutableClass.newInstance();
		return master.getInitalData();
	}

	@Deprecated
	public Message<?> handleRegistration(Object registrationObject) {
		LOG.info("Got registration request");
		Task<?> task = new SimpleTask<>(TaskId.newInstance(),
				registrationObject);
		Message<?> message = new Message<>(MessageType.REGISTRATION_RESPONSE,
				task);
		currentTask = null;
		return message;
	}

	@Deprecated
	public Message<?> handleWorkInit() {
		LOG.debug("Got work init request");
		Message<?> message = null;
		try {
			currentTask = taskEndpoint.getTask();
			message = new Message<>(MessageType.WORK_INIT_RESPONSE, currentTask);
		} catch (InterruptedException e) {
			LOG.debug("Got InterruptedException, stopping work");
			currentTask = null;
			message = new Message<>(MessageType.SHUTDOWN, null);
		}
		return message;
	}

	@Deprecated
	public Message<?> handleWork(Message<?> incomingMessage) {
		LOG.debug("Got work request");

		Message<?> message = null;
		Task<?> result = incomingMessage.getTask();
		try {
			// taskEndpoint.storeResult(result.getTaskId(), result.getData());
			currentTask = taskEndpoint.getTask();
			message = new Message<>(MessageType.WORK_RESPONSE, currentTask);
		} catch (InterruptedException e) {
			LOG.debug("Got InterruptedException, stopping work");
			currentTask = null;
			message = new Message<>(MessageType.SHUTDOWN, null);
		}
		return (message);
	}

	@SuppressWarnings("unchecked")
	public Task<T> getCurrentTask() {
		return (Task<T>) currentTask;
	}

	// private <T> Message<T> receive() throws CommunicationException {
	// try {
	// return endpoint.receive();
	// } catch (ReceiveException e) {
	// LOG.error("Could not recevie message", e);
	// throw new CommunicationException(e);
	// }
	// }
	//
	// private void send(Message<?> message) throws CommunicationException {
	// try {
	// endpoint.send(message);
	// } catch (SendException e) {
	// LOG.error("Could not send message {}", message, e);
	// if (currentTask != null) {
	// try {
	// taskEndpoint.reschedule(currentTask.getTaskId());
	// } catch (InterruptedException e1) {
	// LOG.error("Could not reschedule task at {}", currentTask, e);
	// }
	// }
	// throw new CommunicationException(e);
	// }
	// }
}
