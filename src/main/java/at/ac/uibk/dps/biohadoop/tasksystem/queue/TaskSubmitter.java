package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.util.List;

/**
 * A <tt>TaskSubmitter</tt> is the main entry point to Biohadoops task system. It
 * provides methods to initialize the asynchronous computation of tasks.
 * 
 * @author Christian Gapp
 *
 * @param <T>
 *            The type of data, that is submitted to the Task system
 * @param <S>
 *            The result type for the {@link TaskFuture}, that is returned by
 *            this add() and addAll() methods
 */
public interface TaskSubmitter<T, S> {

	/**
	 * Submit a piece of work, consisting of a chunk of data, to the task
	 * system, where it can be distributed to the workers for asynchronous
	 * computation.
	 * 
	 * @param data
	 *            chunk of data, that should be submitted to the Task system
	 * @return {@link TaskFuture} that represents the result of an asynchronous
	 *         computation
	 * @throws TaskException
	 *             if there was some error in the Task system
	 */
	public TaskFuture<S> add(T data) throws TaskException;

	/**
	 * Submit a list of work items, consisting of chunks of data, to the Task
	 * system, where it can be distributed to the workers asynchronous
	 * computation.
	 * 
	 * @param datas
	 *            Chunks of data, that should be submitted to the Task system
	 * @return A list of {@link TaskFutures} each one representing the result of
	 *         exactly one asynchronous computation
	 * @throws TaskException
	 *             if there was some error in the Task system
	 */
	public List<TaskFuture<S>> addAll(List<T> datas) throws TaskException;

	/**
	 * Submit an array of work items, consisting of chunks of data, to the Task
	 * system, where it can be distributed to the workers asynchronous
	 * computation.
	 * 
	 * @param datas
	 *            Chunks of data, that should be submitted to the Task system
	 * @return A list of {@link TaskFutures} each one representing the result of
	 *         exactly one asynchronous computation
	 * @throws TaskException
	 *             if there was some error in the Task system
	 */
	public List<TaskFuture<S>> addAll(T[] datas) throws TaskException;

}
