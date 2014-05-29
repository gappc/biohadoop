package at.ac.uibk.dps.biohadoop.jobmanager.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.jobmanager.Job;
import at.ac.uibk.dps.biohadoop.jobmanager.JobId;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.TaskId;
import at.ac.uibk.dps.biohadoop.jobmanager.TaskRequest;
import at.ac.uibk.dps.biohadoop.jobmanager.TaskState;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobResponse;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobState;
import at.ac.uibk.dps.biohadoop.jobmanager.handler.SimpleJobHandler;

public class TaskQueue<T> extends SimpleJobHandler<T> {

	private static final Logger LOG = LoggerFactory.getLogger(TaskQueue.class);

	private final String queueName;
	
	private Map<TaskId, Job<T>> taskToJob = new ConcurrentHashMap<>();
	private BlockingQueue<QueueEntry<T>> todo = new LinkedBlockingQueue<QueueEntry<T>>();
	private Map<Thread, Integer> threads = new ConcurrentHashMap<>();
	private AtomicBoolean queueStopped = new AtomicBoolean(false);

	public TaskQueue(String name) {
		this.queueName = name;
	}

	public boolean addJob(Job<T> job) {
		if (queueStopped.compareAndSet(true, true)) {
			return false;
		}
		job.addJobHandler(this);
		List<TaskRequest<T>> taskRequests = job.getTaskRequests();
		try {
			for (TaskRequest<T> taskRequest : taskRequests) {
				taskToJob.put(taskRequest.getTask().getTaskId(), job);
				todo.put(new QueueEntry<T>(job, taskRequest));
			}
			job.setJobState(JobState.SUBMITTED);
			return true;
		} catch (InterruptedException e) {
			LOG.error("Could not add Job for JobRequest {}", job, e);
			for (TaskRequest<T> taskRequest : taskRequests) {
				TaskId taskId = taskRequest.getTask().getTaskId();
				todo.remove(taskId);
				taskToJob.remove(taskId);
			}
			job.setJobState(JobState.ERROR);
		}
		return false;
	}

	public Task<T> next() {
		if (queueStopped.compareAndSet(true, true)) {
			return null;
		}
		
		QueueEntry<T> nextEntry;
		Thread thread = Thread.currentThread();
		threads.put(thread, 0);
		try {
			nextEntry = todo.take();
			Job<T> job = nextEntry.getJob();
			TaskRequest<T> taskRequest = nextEntry.getTaskRequest();
			TaskId taskId = taskRequest.getTask().getTaskId();
			job.setTaskState(taskId, TaskState.RUNNING);
			job.setJobState(JobState.RUNNING);
			return taskRequest.getTask();
		} catch (InterruptedException e) {
			LOG.warn("Got InterruptedException on queue {} for thread {}, assuming to shut down", queueName, thread.getName());
			return null;
		}
		finally {
			threads.remove(thread);
		}
	}

	public void reschedule(TaskId taskId) throws InterruptedException {
		Job<T> job = taskToJob.get(taskId);
		TaskRequest<T> taskRequest = job.getTaskRequest(taskId);
		job.setTaskState(taskId, TaskState.NEW);
		todo.put(new QueueEntry<T>(job, taskRequest));
	}
	
	public Job<T> getJobForTask(TaskId taskId) {
		return taskToJob.get(taskId);
	}
	
	@Override
	public void onFinished(JobResponse<T> jobResponse) {
		List<TaskId> taskIds = new ArrayList<>();
		JobId jobId = jobResponse.getJobId();
		for (Entry<TaskId, Job<T>> entry : taskToJob.entrySet()){
			if (entry.getValue().getJobId().equals(jobId)) {
				taskIds.add(entry.getKey());
			}
		}
		for (TaskId taskId : taskIds) {
			taskToJob.remove(taskId);		
		}
	}
	
	public void killWaitingTasks() {
		LOG.info("Killing all waiting Tasks");
		queueStopped.set(true);
		for (Thread thread : threads.keySet()) {
			thread.interrupt();
		}
	}

}
