package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.io.Serializable;

public class TaskConfiguration<T> implements Serializable {

	private static final long serialVersionUID = 4860155334607735732L;
	
	private TaskTypeId taskTypeId;
	private String asyncComputableClassName;
	private T initialData;

	public TaskConfiguration() {
		// Needed for serialization
	}
	
	public TaskConfiguration(String asyncComputableClassName, T initialData) {
		this.taskTypeId = getTaskTypeId(asyncComputableClassName, initialData);
		this.asyncComputableClassName = asyncComputableClassName;
		this.initialData = initialData;
	}
	
	public TaskTypeId getTaskTypeId() {
		return taskTypeId;
	}

	public String getAsyncComputableClassName() {
		return asyncComputableClassName;
	}

	public T getInitialData() {
		return initialData;
	}
	
	private TaskTypeId getTaskTypeId(
			String asyncComputableClassName, T initialData) {
		Long hash = new Long(asyncComputableClassName.hashCode() * 13);
		if (initialData != null) {
			hash += initialData.hashCode() * 17;
		}
		return new TaskTypeId(hash);
	}

}
