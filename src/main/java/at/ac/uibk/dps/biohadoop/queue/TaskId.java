package at.ac.uibk.dps.biohadoop.queue;

import java.io.Serializable;
import java.util.UUID;

public class TaskId implements Serializable {

	private static final long serialVersionUID = 6854822752868421064L;
	
	private final UUID id;
	
	private TaskId() {
		this.id = UUID.randomUUID();
	}
	
	public static TaskId newInstance() {
		return new TaskId();
	}
	
	public UUID getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TaskId)) {
			return false;
		}
		TaskId taskId = (TaskId) obj;
		return this.id.equals(taskId.id);
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	@Override
	public String toString() {
		return id.toString();
	}
}