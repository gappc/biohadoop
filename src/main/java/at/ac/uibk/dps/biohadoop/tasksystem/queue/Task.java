package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.io.Serializable;

import at.ac.uibk.dps.biohadoop.tasksystem.AsyncComputable;
import at.ac.uibk.dps.biohadoop.tasksystem.submitter.TaskSubmitter;

/**
 * A task, that is used by Biohadoop's Task system. Methods are provided to get
 * the {@link TaskId} and the wrapped data, that is submitted to the Task system
 * by an algorithm author through a {@link TaskSubmitter}.
 * 
 * @author Christian Gapp
 *
 * @param <T>
 */
public class Task<T> implements Serializable {

	private static final long serialVersionUID = -3520833985789659499L;

	private TaskId taskId;
	private TaskTypeId taskTypeId;
	private T data;

	public Task() {
		// Nothing to do
	}

	/**
	 * Creates a <tt>SimpleTask</tt> that is used in the Task system
	 * 
	 * @param taskId
	 *            the {@link TaskId} for this task
	 * @param data
	 *            that should be wrapped inside this task and send to the
	 *            workers
	 */
	public Task(TaskId taskId, TaskTypeId taskTypeId, T data) {
		this.taskId = taskId;
		this.taskTypeId = taskTypeId;
		this.data = data;
	}

	/**
	 * Gets the {@link TaskId} for this task
	 * 
	 * @return the {@link TaskId} for this task
	 */
	public TaskId getTaskId() {
		return taskId;
	}

	public void setTaskId(TaskId taskId) {
		this.taskId = taskId;
	}

	/**
	 * Gets the {@link TaskTypeId} of this task. This TaskTypeId uniquely
	 * identifies, which {@link AsyncComputable} and initalData should be used
	 * to compute the result of the task.
	 * 
	 * @return the {@link TaskTypeId}
	 */
	public TaskTypeId getTaskTypeId() {
		return taskTypeId;
	}

	public void setTaskTypeId(TaskTypeId taskTypeId) {
		this.taskTypeId = taskTypeId;
	}

	/**
	 * Gets the wrapped data for this task, that is submitted to the Task system
	 * by an algorithm author through a {@link TaskSubmitter}. This data is also
	 * send to the workers for their computation.
	 * 
	 * @return the wrapped data
	 */
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return taskId + " | " + data;
	}

}
