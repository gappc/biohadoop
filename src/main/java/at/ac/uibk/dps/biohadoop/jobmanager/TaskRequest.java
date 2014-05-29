package at.ac.uibk.dps.biohadoop.jobmanager;

public class TaskRequest<T> {

	private final Task<T> task;
	private final int slot;
	private TaskState taskState = TaskState.NEW;

	public TaskRequest(Task<T> task, int slot) {
		this.task = task;
		this.slot = slot;
	}

	public Task<T> getTask() {
		return task;
	}

	public int getSlot() {
		return slot;
	}

	public TaskState getTaskState() {
		return taskState;
	}

	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
	}

}
