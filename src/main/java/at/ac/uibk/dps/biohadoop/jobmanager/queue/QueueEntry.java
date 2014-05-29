package at.ac.uibk.dps.biohadoop.jobmanager.queue;

import at.ac.uibk.dps.biohadoop.jobmanager.Job;
import at.ac.uibk.dps.biohadoop.jobmanager.TaskRequest;

public class QueueEntry<T> {
	private final Job<T> job;
	private final TaskRequest<T> taskRequest;

	public QueueEntry(Job<T> job, TaskRequest<T> taskRequest) {
		this.job = job;
		this.taskRequest = taskRequest;
	}

	public Job<T> getJob() {
		return job;
	}

	public TaskRequest<T> getTaskRequest() {
		return taskRequest;
	}
}