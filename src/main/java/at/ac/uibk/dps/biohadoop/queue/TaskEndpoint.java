package at.ac.uibk.dps.biohadoop.queue;

/**
 * An entry point for the consumers of tasks, e.g. for Master Endpoints. Methods
 * are provided to get a task, to store the result of a computation and to
 * reschedule an already existing task, e.g. in the case of task failure.
 * 
 * @author Christian Gapp
 *
 * @param <T>
 *            The type of data, that is read out of the task queue
 * @param <S>
 *            The result type of an asynchronous computation
 */
public interface TaskEndpoint<T, S> {

	/**
	 * Get a task
	 * 
	 * @return a task
	 * @throws InterruptedException
	 */
	public Task<T> getTask() throws TaskException, ShutdownException;

	/**
	 * @param taskId
	 * @param data
	 * @throws InterruptedException
	 */
	public void storeResult(TaskId taskId, S data) throws TaskException, ShutdownException;

	/**
	 * @param taskId
	 * @throws InterruptedException
	 */
	public void reschedule(TaskId taskId) throws TaskException, ShutdownException;
}
