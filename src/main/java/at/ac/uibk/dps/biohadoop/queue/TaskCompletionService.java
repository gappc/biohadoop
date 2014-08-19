package at.ac.uibk.dps.biohadoop.queue;

import java.util.List;

/**
 * Convinience class with a method to wait for a list of {@link TaskFuture}.
 * 
 * @author Christian Gapp
 *
 */
public class TaskCompletionService {

	private TaskCompletionService() {
		// Nothing to do
	}

	/**
	 * Wait for all {@link TaskFuture} in the list for completion. Only if all
	 * tasks are completed, the method returns.
	 * 
	 * @param taskFutures
	 *            list of {@link TaskFuture} to wait for completion
	 * @throws TaskException
	 *             if an error happens while waiting for a task
	 */
	public static <T> void awaitAll(List<TaskFuture<T>> taskFutures)
			throws TaskException {
		for (TaskFuture<T> taskFuture : taskFutures) {
			taskFuture.get();
		}
	}
}
