package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.io.Serializable;

/**
 * Base implementation of {@link Task}. It provides methods to get and set the
 * {@link TaskId} and the data for this task.
 * 
 * @author Christian Gapp
 *
 * @param <T>
 */
public class SimpleTask<T> implements Task<T>, Serializable {

	private static final long serialVersionUID = -3520833985789659499L;

	private TaskId taskId;
	private T data;

	public SimpleTask() {
		// Nothing to do
	}

	/**
	 * Creates a <tt>SimpleTask</tt> that is used in the Task system
	 * 
	 * @param taskId the {@link TaskId} for this task
	 * @param data that should be wrapped inside this task and send to the workers
	 */
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
