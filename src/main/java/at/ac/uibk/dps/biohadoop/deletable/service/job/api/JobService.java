package at.ac.uibk.dps.biohadoop.deletable.service.job.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.deletable.Job;
import at.ac.uibk.dps.biohadoop.deletable.JobId;
import at.ac.uibk.dps.biohadoop.deletable.Task;
import at.ac.uibk.dps.biohadoop.deletable.TaskId;
import at.ac.uibk.dps.biohadoop.deletable.service.job.queue.TaskQueue;

public class JobService<T, S> {

	private static final Logger LOG = LoggerFactory.getLogger(JobService.class);

	@SuppressWarnings("rawtypes")
	private static final JobService JOB_MANAGER = new JobService();

	private Map<String, TaskQueue<T, S>> queues = new HashMap<>();
	// TODO if jobs are not removed, than a memory leak may occur. check e.g.
	// weakhashmap or guava mapmaker
	private Map<JobId, Job<T, S>> jobs = new ConcurrentHashMap<>();

	private JobService() {
	}

	@SuppressWarnings("unchecked")
	public static <U, V> JobService<U, V> getInstance() {
		return JobService.JOB_MANAGER;
	}

	public JobId submitJob(JobRequest<T> jobRequest, String queueName) {
		JobId jobId = JobId.newInstance();
		LOG.debug("Submitting Job {} to queue", jobId);
		Job<T, S> job = new Job<T, S>(jobId, jobRequest);

		TaskQueue<T, S> queue = getTaskQueue(queueName);
		boolean hasAdded = queue.addJob(job);
		if (hasAdded) {
			jobs.put(jobId, job);
			return job.getJobId();
		}
		return null;
	}

	public boolean reschedule(Task<T> task, String queueName) {
		LOG.info("Rescheduling task {}", task);
		TaskQueue<T, S> queue = getTaskQueue(queueName);
		return queue.reschedule(task.getTaskId());
	}

	public Job<T, S> jobCleanup(JobId jobId) {
		LOG.debug("Removing Job with Id {} from internal map", jobId);
		return jobs.remove(jobId);
	}

	public JobReport getReport(JobId jobId) {
		Job<T, S> job = jobs.get(jobId);
		JobReport jobReport = new JobReport(job.getState());
		return jobReport;
	}

	public Task<T> getTask(String queueName) {
		TaskQueue<T, S> queue = getTaskQueue(queueName);
		Task<T> task = queue.next();
		LOG.debug("Getting Task {} for work", task);
		return task;
	}

	public void putResult(Task<S> task, String queueName) {
		LOG.debug("Returning result for Task {}", task);
		TaskId taskId = task.getTaskId();
		TaskQueue<T, S> queue = getTaskQueue(queueName);
		Job<T, S> job = queue.getJobForTask(taskId);
		job.addResult(task);
	}

	public JobResponse<S> getJobResponse(final JobId jobId) {
		Job<T, S> job = jobs.get(jobId);
		if (job == null) {
			throw new IllegalArgumentException("Could not find job with id "
					+ jobId);
		}
		return job.getResult();
	}

	public void shutdown() {
		LOG.info("Shutting down JobService");
		for (String queueName : queues.keySet()) {
			TaskQueue<T, S> taskQueue = queues.get(queueName);
			taskQueue.killWaitingTasks();
		}
	}

	private TaskQueue<T, S> getTaskQueue(String queueName) {
		TaskQueue<T, S> queue = queues.get(queueName);
		if (queue == null) {
			synchronized (queues) {
				queue = queues.get(queueName);
				if (queue == null) {
					LOG.info("Instanciated new queue for {}", queueName);
					queue = new TaskQueue<T, S>(queueName);
					queues.put(queueName, queue);
				}
			}
		}
		return queue;
	}

}
