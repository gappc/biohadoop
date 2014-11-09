package at.ac.uibk.dps.biohadoop.tasksystem.queue;

/**
 * Defines methods to get the default task queue. It is strongly suggested to
 * use {@link #getTaskQueue()} if the task queue is needed inside Biohadoop.
 * 
 * @author Christian Gapp
 *
 */
public class TaskQueueService {

	private static final TaskQueue QUEUE = new TaskQueue<>();

	private TaskQueueService() {
		// Nothing to do
	}

	/**
	 * Returns the default task queue
	 * 
	 * @return the {@link TaskQueue}
	 */
	public static <R, T, S> TaskQueue<R, T, S> getTaskQueue() {
		return QUEUE;
	}

}
