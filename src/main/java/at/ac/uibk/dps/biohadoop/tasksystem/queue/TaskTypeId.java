package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.io.Serializable;

public class TaskTypeId implements Serializable {

	private static final long serialVersionUID = 2877586644741563861L;

	public Long id;

	public TaskTypeId() {
		// Needed for serialization
	}
	
	public TaskTypeId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TaskTypeId)) {
			return false;
		}
		TaskTypeId taskTypeId = (TaskTypeId) obj;
		return id == taskTypeId.id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
