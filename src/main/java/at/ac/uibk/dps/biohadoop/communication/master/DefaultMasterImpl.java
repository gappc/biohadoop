package at.ac.uibk.dps.biohadoop.communication.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.queue.ShutdownException;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskException;
import at.ac.uibk.dps.biohadoop.queue.TaskId;

public class DefaultMasterImpl<R, T, S> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultMasterImpl.class);

	private final TaskEndpoint<R, T, S> taskEndpoint;
	private Task<T> currentTask = null;

	public DefaultMasterImpl(String settingName) {
		taskEndpoint = new TaskEndpointImpl<>(settingName);
	}

	public Message<T> handleMessage(Message<S> inputMessage)
			throws HandleMessageException {
		switch (inputMessage.getType()) {
		case NONE:
			break;
		case REGISTRATION_REQUEST:
			return (Message<T>) getInitialData(inputMessage);
		case WORK_INIT_REQUEST:
			return getWorkResponse();
		case WORK_REQUEST:
			storeResult(inputMessage);
			return getWorkResponse();
		default:
			currentTask = null;
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
			TaskId taskId = inputMessage.getTask().getTaskId();
			R initialData = readInitialData(taskId);
			String className = ((ClassNameWrappedTask<T>) inputMessage
					.getTask()).getClassName();
			// Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass
			// = (Class<? extends RemoteExecutable<R, T, S>>) Class
			// .forName(className);
			Task<R> task = new ClassNameWrappedTask<>(taskId, initialData,
					className);
			return new Message<>(MessageType.REGISTRATION_RESPONSE, task);
		} catch (TaskException e) {
			throw new HandleMessageException("Could not get initial data", e);
		}

	}

	private void storeResult(Message<S> inputMessage)
			throws HandleMessageException {
		Task<S> task = inputMessage.getTask();
		try {
			taskEndpoint.storeResult(task.getTaskId(), task.getData());
		} catch (TaskException e) {
			throw new HandleMessageException(
					"Error while storing result for message {}" + inputMessage);
		} catch (ShutdownException e) {
			throw new HandleMessageException(
					"Got ShutdownException, assuming this means to stop stopping work");
		}
	}

	private Message<T> getWorkResponse() {
		try {
			Task<T> task = taskEndpoint.getTask();
			currentTask = task;
			return new Message<>(MessageType.WORK_INIT_RESPONSE, task);
		} catch (TaskException | ShutdownException e) {
			LOG.debug("Got TaskException, assuming this means to stop stopping work");
			currentTask = null;
			return new Message<>(MessageType.SHUTDOWN, null);
		}
	}

	private R readInitialData(TaskId taskId) throws TaskException {
		return taskEndpoint.getInitialData(taskId);
	}

	public Task<T> getCurrentTask() {
		return (Task<T>) currentTask;
	}
}
