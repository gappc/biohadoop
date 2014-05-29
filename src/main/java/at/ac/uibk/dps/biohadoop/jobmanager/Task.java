package at.ac.uibk.dps.biohadoop.jobmanager;

public class Task<T> {

	private final TaskId taskId;
	private final T data;

	public Task(TaskId taskId, T data) {
		this.taskId = taskId;
		this.data = data;
	}

	public TaskId getTaskId() {
		return taskId;
	}

	public T getData() {
		return data;
	}

	@Override
	public String toString() {
		return taskId.toString();
	}
}
