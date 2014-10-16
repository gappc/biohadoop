package at.ac.uibk.dps.biohadoop.tasksystem.queue;

/**
 * Simple POJO that represents a single entry for a task queue map. Usually,
 * this map is used as a store for currently submitted tasks
 * 
 * @author Christian Gapp
 *
 * @param <T>
 *            Type for the task data i.e. type of data that is used in
 *            asynchronous computation
 * @param <S>
 *            Type of the result of an asynchronous computation
 */
public class TaskQueueEntry<R, T, S> {

	private final Task<T> task;
	private final TaskFutureImpl<S> taskFuture;
	private final TaskConfiguration<R> taskConfiguration;

	public TaskQueueEntry(Task<T> task, TaskFutureImpl<S> taskFuture,
			TaskConfiguration<R> taskConfiguration) {
		this.task = task;
		this.taskFuture = taskFuture;
		this.taskConfiguration = taskConfiguration;
	}

	public Task<T> getTask() {
		return task;
	}

	public TaskFutureImpl<S> getTaskFutureImpl() {
		return taskFuture;
	}

	public TaskConfiguration<R> getTaskConfiguration() {
		return taskConfiguration;
	}

}
