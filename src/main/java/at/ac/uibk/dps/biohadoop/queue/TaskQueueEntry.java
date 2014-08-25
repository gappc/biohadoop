package at.ac.uibk.dps.biohadoop.queue;

/**
 * Simple POJO that represents A single entry for a task queue map. Usually,
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
	private final R initialData;

	public TaskQueueEntry(Task<T> task, TaskFutureImpl<S> taskFuture, R initalData) {
		this.task = task;
		this.taskFuture = taskFuture;
		this.initialData = initalData;
	}

	public Task<T> getTask() {
		return task;
	}

	public TaskFutureImpl<S> getTaskFutureImpl() {
		return taskFuture;
	}

	public R getInitialData() {
		return initialData;
	}

}
