package at.ac.uibk.dps.biohadoop.tasksystem.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.ShutdownException;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskException;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskId;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueue;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueueService;

public class TaskConsumer<R, T, S> {

	private static final Logger LOG = LoggerFactory
			.getLogger(TaskConsumer.class);

	private final String settingName;
	private final TaskQueue<R, T, S> taskQueue;
	private Task<T> currentTask = null;

	public TaskConsumer(String settingName) {
		this.settingName = settingName;
		taskQueue = TaskQueueService.getInstance().<R, T, S> getTaskQueue(
				settingName);
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
	
	public R getInitialData(TaskId taskId) throws TaskException {
		return taskQueue.getInitialData(taskId);
	}

	public Task<T> getCurrentTask() {
		return (Task<T>) currentTask;
	}
	
	public void reschedule(TaskId taskId) throws ShutdownException {
		try {
			taskQueue.reschedule(taskId);
		} catch (InterruptedException e) {
			throw new ShutdownException("Got interrupted while rescheduling task "
					+ taskId + " to queue " + settingName);
		} catch (TaskException e) {
			LOG.error("Could nor reschedule task {}", taskId, e);
		}
	}

	public void storeResult(TaskId taskId, S data) throws TaskException,
			ShutdownException {
		try {
			taskQueue.storeResult(taskId, data);
		} catch (TaskException e) {
			throw new ShutdownException("Error while storing task " + taskId
					+ " to queue " + settingName);
		}
	}
	
	private Message<R> getInitialData(Message<S> inputMessage)
			throws HandleMessageException {
		LOG.info("Got registration request");
		try {
			TaskId taskId = inputMessage.getTask().getTaskId();
			R initialData = readInitialData(taskId);
			String className = ((ClassNameWrappedTask<T>) inputMessage
					.getTask()).getClassName();
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
			taskQueue.storeResult(task.getTaskId(), task.getData());
		} catch (TaskException e) {
			throw new HandleMessageException(
					"Error while storing result for message {}" + inputMessage);
		}
	}

	private Message<T> getWorkResponse() {
		try {
			Task<T> task = taskQueue.getTask();
			currentTask = task;
			return new Message<>(MessageType.WORK_INIT_RESPONSE, task);
		} catch (InterruptedException e) {
			LOG.debug("Got InterruptedException, assuming this means to stop stopping work");
			currentTask = null;
			return new Message<>(MessageType.SHUTDOWN, null);
		}
	}

	private R readInitialData(TaskId taskId) throws TaskException {
		return taskQueue.getInitialData(taskId);
	}

}
