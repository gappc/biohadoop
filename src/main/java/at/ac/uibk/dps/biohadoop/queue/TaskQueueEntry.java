package at.ac.uibk.dps.biohadoop.queue;

public class TaskQueueEntry<T, S> {

	private final Task<T> task;
	private final TaskFutureImpl<S> taskFuture;

	public TaskQueueEntry(Task<T> task, TaskFutureImpl<S> taskFuture) {
		this.task = task;
		this.taskFuture = taskFuture;
	}

	public Task<T> getTask() {
		return task;
	}

	public TaskFutureImpl<S> getTaskFutureImpl() {
		return taskFuture;
	}

}
