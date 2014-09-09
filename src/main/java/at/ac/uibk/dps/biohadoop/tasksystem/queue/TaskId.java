package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.io.Serializable;
import java.util.UUID;

/**
 * Id for tasks, that is used in different parts of Biohadoops Task system. Uses
 * {@link UUID} to return unique id values.
 * 
 * @author Christian Gapp
 *
 */
public class TaskId implements Serializable {

	private static final long serialVersionUID = 6854822752868421064L;

	private final UUID id;

	private TaskId() {
		this.id = UUID.randomUUID();
	}
	
	private TaskId(String taskId) {
		this.id = UUID.fromString(taskId);
	}

	/**
	 * Creates a new TaskId object with a random id
	 * 
	 * @return new TaskId object with a random id
	 */
	public static TaskId newInstance() {
		return new TaskId();
	}
	
	public static TaskId newInstance(String taskId) {
		return new TaskId(taskId);
	}

	/**
	 * Get the unique id
	 * 
	 * @return the unique id
	 */
	public UUID getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TaskId)) {
			return false;
		}
		TaskId taskId = (TaskId) obj;
		return id.equals(taskId.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id.toString();
	}
}
