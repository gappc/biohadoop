package at.ac.uibk.dps.biohadoop.queue;

/**
 * An entry point for the consumers of tasks, e.g. for Master Endpoints. Methods
 * are provided to get a task, to store the result of a computation and to
 * reschedule an already existing task, e.g. in the case of task failure.
 * 
 * @author Christian Gapp
 *
 * @param <T>
 *            The type of data, that is read
 * @param <S>
 *            The result type of an asynchronous computation
 */
public interface TaskEndpoint<T, S> {

	/**
	 * Get a task
	 * 
	 * @return a task that can be used for further purposes
	 * @throws TaskException
	 *             if there is an error while getting a task
	 * @throws ShutdownException
	 *             if the consumer should be notified that Biohadoop wants to
	 *             shut down
	 */
	public Task<T> getTask() throws TaskException, ShutdownException;

	/**
	 * Stores the result of an asynchronous computation. If the taskId is
	 * unknown, a {@link TaskException} should be raised.
	 * 
	 * @param taskId
	 *            the {@link TaskId} of the task, whose result is returned
	 * @param data
	 *            the data of the task, whose result is returned
	 * @throws TaskException
	 *             if there is an error while getting a task
	 * @throws ShutdownException
	 *             if the consumer should be notified that Biohadoop wants to
	 *             shut down
	 */
	public void storeResult(TaskId taskId, S data) throws TaskException,
			ShutdownException;

	/**
	 * Reschedules an already existing task, e.g. in the case of failure. If the
	 * taskId is unknown, a {@link TaskException} is raised.
	 * 
	 * @param taskId
	 *            the {@link TaskId} of the task, that should be rescheduled
	 * @throws TaskException
	 *             if there is an error while getting a task
	 * @throws ShutdownException
	 *             if the consumer should be notified that Biohadoop wants to
	 *             shut down
	 */
	public void reschedule(TaskId taskId) throws TaskException,
			ShutdownException;
}
