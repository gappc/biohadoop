package at.ac.uibk.dps.biohadoop.jobmanager.queue;

import at.ac.uibk.dps.biohadoop.jobmanager.Job;
import at.ac.uibk.dps.biohadoop.jobmanager.TaskRequest;

public class QueueEntry<T, S> {
	private final Job<T, S> job;
	private final TaskRequest<T> taskRequest;

	public QueueEntry(Job<T, S> job, TaskRequest<T> taskRequest) {
		this.job = job;
		this.taskRequest = taskRequest;
	}

	public Job<T, S> getJob() {
		return job;
	}

	public TaskRequest<T> getTaskRequest() {
		return taskRequest;
	}
}