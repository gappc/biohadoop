package at.ac.uibk.dps.biohadoop.tasksystem.queue;

/**
 * Simple POJO that represents a single entry for a task queue map. Usually,
 * this map is used as a store for currently submitted tasks
 * 
 * @author Christian Gapp
 */
public class TaskQueueEntry {

	private final Task<?> task;
	private final TaskFutureImpl<?> taskFuture;
	private final TaskConfiguration<?> taskConfiguration;

	public TaskQueueEntry(Task<?> task, TaskFutureImpl<?> taskFuture,
			TaskConfiguration<?> taskConfiguration) {
		this.task = task;
		this.taskFuture = taskFuture;
		this.taskConfiguration = taskConfiguration;
	}

	public Task<?> getTask() {
		return task;
	}

	public TaskFutureImpl<?> getTaskFutureImpl() {
		return taskFuture;
	}

	public TaskConfiguration<?> getTaskConfiguration() {
		return taskConfiguration;
	}

}
