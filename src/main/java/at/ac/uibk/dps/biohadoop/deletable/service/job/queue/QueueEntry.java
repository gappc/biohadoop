package at.ac.uibk.dps.biohadoop.deletable.service.job.queue;

import at.ac.uibk.dps.biohadoop.deletable.Job;
import at.ac.uibk.dps.biohadoop.deletable.TaskRequest;

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