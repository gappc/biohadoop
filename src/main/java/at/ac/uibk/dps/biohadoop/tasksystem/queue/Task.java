package at.ac.uibk.dps.biohadoop.tasksystem.queue;

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
public interface Task<T> {

	/**
	 * Gets the {@link TaskId} for this task
	 * 
	 * @return the {@link TaskId} for this task
	 */
	public TaskId getTaskId();

	/**
	 * Gets the wrapped data for this task, that is submitted to the Task system
	 * by an algorithm author through a {@link TaskSubmitter}. This data is also
	 * send to the workers for their computation.
	 * 
	 * @return the wrapped data
	 */
	public T getData();

}