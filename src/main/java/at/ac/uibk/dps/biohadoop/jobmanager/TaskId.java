package at.ac.uibk.dps.biohadoop.jobmanager;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class TaskId implements Serializable {

	private static final long serialVersionUID = -7342309576690203270L;
	
	private final int id;
	
	private TaskId() {
		this.id = ThreadLocalRandom.current().nextInt();
	}
	
	public static TaskId newInstance() {
		return new TaskId();
	}

	public long getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TaskId)) {
			return false;
		}
		TaskId jobId = (TaskId) obj;
		return this.id == jobId.id;
	}
	
	@Override
	public int hashCode() {
		return this.id;
	}
	
	@Override
	public String toString() {
		return Integer.toString(id);
	}
}
