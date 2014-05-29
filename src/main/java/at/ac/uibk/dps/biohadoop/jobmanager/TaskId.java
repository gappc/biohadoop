package at.ac.uibk.dps.biohadoop.jobmanager;

import java.util.Random;

public class TaskId {
	private static final Random rand = new Random();
	private final int id;
	
	private TaskId() {
		this.id = TaskId.rand.nextInt();
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
}
