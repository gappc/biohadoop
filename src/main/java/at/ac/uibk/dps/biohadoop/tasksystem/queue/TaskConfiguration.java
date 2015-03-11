package at.ac.uibk.dps.biohadoop.tasksystem.queue;

import java.io.Serializable;

public class TaskConfiguration<T> implements Serializable {

	private static final long serialVersionUID = 4860155334607735732L;
	
	private TaskTypeId taskTypeId;
	private String workerClassName;
	private T initialData;

	public TaskConfiguration() {
		// Needed for serialization
	}
	
	public TaskConfiguration(String workerClassName, T initialData) {
		this.taskTypeId = getTaskTypeId(workerClassName, initialData);
		this.workerClassName = workerClassName;
		this.initialData = initialData;
	}
	
	public TaskTypeId getTaskTypeId() {
		return taskTypeId;
	}

	public String getWorkerClassName() {
		return workerClassName;
	}

	public T getInitialData() {
		return initialData;
	}
	
	private TaskTypeId getTaskTypeId(
			String workerClassName, T initialData) {
		Long hash = new Long(workerClassName.hashCode() * 13);
		if (initialData != null) {
			hash += initialData.hashCode() * 17;
		}
		return new TaskTypeId(hash);
	}

}
