package at.ac.uibk.dps.biohadoop.queue;

import java.io.Serializable;

public class SimpleTask<T> implements Task<T>, Serializable {

	private static final long serialVersionUID = -3520833985789659499L;
	
	private TaskId taskId;
	private T data;

	public SimpleTask() {
	}
	
	public SimpleTask(TaskId taskId, T data) {
		this.taskId = taskId;
		this.data = data;
	}

	@Override
	public TaskId getTaskId() {
		return taskId;
	}

	@Override
	public T getData() {
		return data;
	}

	@Override
	public String toString() {
		return taskId + " | " + data;
	}

	public void setTaskId(TaskId taskId) {
		this.taskId = taskId;
	}

	public void setData(T data) {
		this.data = data;
	}
	
}
