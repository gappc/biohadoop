package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.io.Serializable;
import java.util.Random;
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
	private static final Random RAND = new Random();

	private final UUID id;

	private TaskId() {
		// UUID uses internally SecureRandom, which generates better random
		// numbers, but is slow. As security is not a concern at this stage, we
		// can use the implementation above, which gives significant speed
		// improvements
		this.id = new UUID(RAND.nextLong(), RAND.nextLong());
	}

	/**
	 * Creates a new TaskId object with a random id
	 * 
	 * @return new TaskId object with a random id
	 */
	public static TaskId newInstance() {
		return new TaskId();
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
