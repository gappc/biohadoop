package at.ac.uibk.dps.biohadoop.jobmanager;

import java.io.Serializable;

public class Task<T> implements Serializable {

	private static final long serialVersionUID = -3520833985789659499L;
	
	private TaskId taskId;
	private T data;

	public Task() {
	}
	
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
		return taskId + " | " + data;
	}
}
