package at.ac.uibk.dps.biohadoop.queue;

/**
 * Base implementation for {@link TaskEndpoint}. It provides methods to get the
 * tasks, to store the results and to reschedule already existing tasks. Uses
 * {@link TaskQueue} as its task source and result destination.
 * 
 * @author Christian Gapp
 *
 * @param <T>
 *            The type of data, that is read out of the task queue
 * @param <S>
 *            The result type of an asynchronous computation
 */
public class TaskEndpointImpl<T, S> implements TaskEndpoint<T, S> {

	private final String queueName;
	private final TaskQueue<T, S> taskQueue;

	/**
	 * Creates an instance of {@link TaskEndpointImpl}, that can be used to get
	 * data out of the named {@link TaskQueue}
	 * 
	 * @param queueName
	 *            the name of the {@link TaskQueue}
	 */
	public TaskEndpointImpl(String queueName) {
		this.queueName = queueName;
		this.taskQueue = TaskQueueService.getInstance().<T, S> getTaskQueue(
				queueName);
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
					+ queueName);
		}
	}

	@Override
	public void storeResult(TaskId taskId, S data) throws TaskException,
			ShutdownException {
		try {
			taskQueue.storeResult(taskId, data);
		} catch (TaskException e) {
			throw new ShutdownException("Error while storing task " + taskId
					+ " to queue " + queueName);
		}
	}

	@Override
	public void reschedule(TaskId taskId) throws TaskException,
			ShutdownException {
		try {
			taskQueue.reschedule(taskId);
		} catch (InterruptedException e) {
			throw new ShutdownException("Error while rescheduling task "
					+ taskId + " to queue " + queueName);
		}
	}
}
