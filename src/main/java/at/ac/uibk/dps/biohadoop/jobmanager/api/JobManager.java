package at.ac.uibk.dps.biohadoop.jobmanager.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.jobmanager.Job;
import at.ac.uibk.dps.biohadoop.jobmanager.JobId;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.TaskId;
import at.ac.uibk.dps.biohadoop.jobmanager.handler.SimpleJobHandler;
import at.ac.uibk.dps.biohadoop.jobmanager.queue.TaskQueue;

public class JobManager<T> extends SimpleJobHandler<T> {

	private static final Logger LOG = LoggerFactory.getLogger(JobManager.class);

	@SuppressWarnings("rawtypes")
	private static final JobManager JOB_MANAGER = new JobManager();

	private Map<String, TaskQueue<T>> queues = new HashMap<>();
	private Map<JobId, Job<T>> jobs = new ConcurrentHashMap<>();

	private JobManager() {
	}

	@SuppressWarnings("unchecked")
	public static <S> JobManager<S> getInstance() {
		return JobManager.JOB_MANAGER;
	}

	public JobId submitJob(JobRequest<T> jobRequest, String queueName) {
		JobId jobId = JobId.newInstance();
		LOG.debug("Submitting Job {} to queue", jobId);
		Job<T> job = new Job<T>(jobId, jobRequest);
		job.addJobHandler(this);

		TaskQueue<T> queue = getTaskQueue(queueName);
		boolean hasAdded = queue.addJob(job);
		if (hasAdded) {
			jobs.put(jobId, job);
			return job.getJobId();
		}
		return null;
	}

	public JobReport getReport(JobId jobId) {
		Job<T> job = jobs.get(jobId);
		JobReport jobReport = new JobReport(job.getState());
		return jobReport;
	}

	public Task<T> getTask(String queueName) {
		TaskQueue<T> queue = getTaskQueue(queueName);
		Task<T> task = queue.next();
		LOG.debug("Getting Task {} for work", task);
		return task;
	}

	public void putResult(Task<T> task, String queueName) {
		LOG.debug("Returning result for Task {}", task);
		TaskId taskId = task.getTaskId();
		TaskQueue<T> queue = getTaskQueue(queueName);
		Job<T> job = queue.getJobForTask(taskId);
		job.addResult(task);
	}

	@Override
	public void onFinished(JobResponse<T> jobResponse) {
		LOG.debug("Removing Job with Id {} from internal map",
				jobResponse.getJobId());
		jobs.remove(jobResponse.getJobId());
	}

	public void shutdown() {
		LOG.info("Shutting down JobManager");
		for (String queueName : queues.keySet()) {
			TaskQueue<T> taskQueue = queues.get(queueName);
			taskQueue.killWaitingTasks();
		}
	}

	private TaskQueue<T> getTaskQueue(String queueName) {
		TaskQueue<T> queue = queues.get(queueName);
		if (queue == null) {
			synchronized (queues) {
				queue = queues.get(queueName);
				if (queue == null) {
					LOG.info("Instanciated new queue for {}", queueName);
					queue = new TaskQueue<T>(queueName);
					queues.put(queueName, queue);
				}
			}
		}
		return queue;
	}

}
