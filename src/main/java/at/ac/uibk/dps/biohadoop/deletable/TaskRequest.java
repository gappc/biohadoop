package at.ac.uibk.dps.biohadoop.deletable;

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("taskId: ").append(task).append(" | state: ")
				.append(taskState);
		return sb.toString();
	}

}
