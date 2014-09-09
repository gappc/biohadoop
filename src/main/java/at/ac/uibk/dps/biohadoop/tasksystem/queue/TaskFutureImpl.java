package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.util.concurrent.CountDownLatch;

/**
 * Represents an asynchronous computation. This class provides a base
 * implementation of {@link TaskFuture}, with methods to see if the computation
 * is complete, and retrieve the result of the computation. The result can only
 * be retrieved when the computation has completed; the {@code get} methods will
 * block if the computation has not yet completed. The {@code set} method sets
 * the result of this object and unblocks the {@code get} method, effectively
 * setting the result for this TaskFutureImpl.
 * 
 * @author Christian Gapp
 *
 * @param <T>
 *            The result type returned by this TaskFutureImpl's {@code get}
 *            methods
 */
public class TaskFutureImpl<T> implements TaskFuture<T> {

	private T data;
	private final CountDownLatch latch = new CountDownLatch(1);

	@Override
	public T get() throws TaskException {
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new TaskException(
					"Error while waiting for TaskFuture to complete", e);
		}
		return data;
	}

	/**
	 * Set the computation result for this object. Sets the result and unblocks
	 * the {@code get} method
	 * 
	 * @param data
	 *            computation result
	 */
	public void set(T data) {
		this.data = data;
		latch.countDown();
	}

	@Override
	public boolean isDone() {
		return latch.getCount() == 0;
	}
}
