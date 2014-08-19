package at.ac.uibk.dps.biohadoop.queue;

/**
 * A task, that is used by Biohadoop's Task system. Methods are provided to get
 * the {@link TaskId} and the wrapped data, that is submitted to the Task system
 * by an algorithm author through a {@link TaskClient}.
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
	 * by an algorithm author through a {@link TaskClient}. Tihs data is also
	 * send to the Worker Endpoints for their computation.
	 * 
	 * @return the wrapped data
	 */
	public T getData();

}
