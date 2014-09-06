package at.ac.uibk.dps.biohadoop.queue;

/**
 * Base implementation for {@link TaskEndpoint}. It provides methods to consume
 * tasks from a task queue, to store the results and to reschedule already
 * existing tasks. Uses {@link TaskQueue} as its task source and result
 * destination.
 * 
 * @author Christian Gapp
 *
 * @param <T>
 *            The type of data, that is consumed from the task queue
 * @param <S>
 *            The result type of an asynchronous computation
 */
public class TaskEndpointImpl<R, T, S> implements TaskEndpoint<R, T, S> {

	private final String settingName;
	private final TaskQueue<R, T, S> taskQueue;

	/**
	 * Creates an instance of {@link TaskEndpointImpl}, that can be used to get
	 * data out of the named {@link TaskQueue}
	 * 
	 * @param settingName
	 *            the name of the setting, which corresponds to the name of the
	 *            {@link TaskQueue}
	 */
	public TaskEndpointImpl(String settingName) {
		this.settingName = settingName;
		this.taskQueue = TaskQueueService.getInstance().<R, T, S> getTaskQueue(
				settingName);
	}

	/**
	 * Gets a task from a {@link TaskQueue}
	 * 
	 * @see TaskEndpoint#getTask()
	 */
	@Override
	public Task<T> getTask() throws TaskException, ShutdownException {
		try {
			return taskQueue.getTask();
		} catch (InterruptedException e) {
			throw new ShutdownException("Error while getting task from queue "
					+ settingName);
		}
	}

	public R getInitialData(TaskId taskId) throws TaskException {
		return taskQueue.getInitialData(taskId);
	}

	@Override
	public void storeResult(TaskId taskId, S data) throws TaskException,
			ShutdownException {
		try {
			taskQueue.storeResult(taskId, data);
		} catch (TaskException e) {
			throw new ShutdownException("Error while storing task " + taskId
					+ " to queue " + settingName);
		}
	}

	@Override
	public void reschedule(TaskId taskId) throws TaskException,
			ShutdownException {
		try {
			taskQueue.reschedule(taskId);
		} catch (InterruptedException e) {
			throw new ShutdownException("Error while rescheduling task "
					+ taskId + " to queue " + settingName);
		}
	}
}
