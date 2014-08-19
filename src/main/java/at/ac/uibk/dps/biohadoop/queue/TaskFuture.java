package at.ac.uibk.dps.biohadoop.queue;

/**
 * A <tt>TaskFuture</tt> represents the result of an asynchronous computation
 * (much like the {@link Future} interface, that is part of the Java standard).
 * Methods are provided to check if the computation is complete, to wait for its
 * completion, and to retrieve the result of the computation. The result can
 * only be retrieved using method <tt>get</tt> when the computation has
 * completed, blocking if necessary until it is ready. Cancellation is not
 * supported.
 * 
 * @author Christian Gapp
 *
 * @param <T>
 *            The result type returned by this TaskFuture's {@code get} methods
 */
public interface TaskFuture<T> {

	/**
	 * Waits if necessary for the computation to complete, and then retrieves
	 * its result.
	 * 
	 * @return the computed result
	 * @throws TaskException
	 *             if there was some error in the Task system
	 */
	public T get() throws TaskException;

	/**
	 * Returns <tt>true</tt> if this task completed, non-blocking
	 * 
	 * @return <tt>true</tt> if this task completed
	 */
	public boolean isDone();

}
