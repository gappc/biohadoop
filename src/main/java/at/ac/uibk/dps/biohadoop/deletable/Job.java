package at.ac.uibk.dps.biohadoop.deletable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.deletable.service.job.api.JobRequest;
import at.ac.uibk.dps.biohadoop.deletable.service.job.api.JobRequestData;
import at.ac.uibk.dps.biohadoop.deletable.service.job.api.JobResponse;
import at.ac.uibk.dps.biohadoop.deletable.service.job.api.JobResponseData;
import at.ac.uibk.dps.biohadoop.deletable.service.job.api.JobState;
import at.ac.uibk.dps.biohadoop.deletable.service.job.handler.JobHandler;

public class Job<T, S> {

	private static final Logger LOG = LoggerFactory.getLogger(Job.class);

	private final JobId jobId;
	private final AtomicBoolean onFinishExecuted = new AtomicBoolean(false);
	private final AtomicInteger openTasksCount = new AtomicInteger();
	private final Map<TaskId, TaskRequest<T>> taskRequests = new ConcurrentHashMap<>();
	private final Map<TaskId, TaskResponse<S>> taskResponses = new ConcurrentHashMap<>();
	private final List<JobHandler<T>> jobHandlers = new ArrayList<>();

	private volatile JobState jobState = JobState.NEW;
	private volatile JobResponse<S> jobResponse;

	public Job(JobId jobId, JobRequest<T> jobRequest) {
		this.jobId = jobId;
		addJobHandler(jobRequest.getHandler());

		openTasksCount.set(jobRequest.getRequestData().size());
		for (JobRequestData<T> requestData : jobRequest.getRequestData()) {
			TaskId taskId = TaskId.newInstance();
			Task<T> task = new Task<T>(taskId, requestData.getData());
			int slot = requestData.getSlot();
			TaskRequest<T> taskRequest = new TaskRequest<T>(task, slot);
			taskRequests.put(taskId, taskRequest);
		}
		setJobState(JobState.NEW);
	}

	public void addJobHandler(JobHandler<T> jobHandler) {
		if (!jobHandlers.contains(jobHandler)) {
			jobHandlers.add(jobHandler);
		}
	}

	public List<TaskRequest<T>> getTaskRequests() {
		List<TaskRequest<T>> result = new ArrayList<>();
		for (TaskId taskId : taskRequests.keySet()) {
			result.add(taskRequests.get(taskId));
		}
		return result;
	}

	public TaskRequest<T> getTaskRequest(TaskId taskId) {
		return taskRequests.get(taskId);
	}

	public void addResult(Task<S> task) {
		TaskId taskId = task.getTaskId();
		TaskRequest<T> taskRequest = taskRequests.get(taskId);

		int slot = taskRequest.getSlot();
		TaskResponse<S> taskResponse = new TaskResponse<>(task, slot);
		taskResponses.put(taskId, taskResponse);

		taskRequest.setTaskState(TaskState.FINISHED);

		openTasksCount.decrementAndGet();
		if (openTasksCount.compareAndSet(0, 0)) {
			LOG.debug(
					"Setting Job {} to finished, openTaskCount = {}, thread= {}, data = {}",
					jobId, openTasksCount, Thread.currentThread(), task);
			createJobResponse();
			setJobState(JobState.FINISHED);
		}
	}
	
	private void createJobResponse() {
		List<JobResponseData<S>> jobResponseDatas = new ArrayList<>();

		for (TaskId taskId : taskResponses.keySet()) {
			TaskResponse<S> taskResponse = taskResponses.get(taskId);
			S data = taskResponse.getData().getData();
			int slot = taskResponse.getSlot();
			JobResponseData<S> responseData = new JobResponseData<S>(data,
					slot);
			jobResponseDatas.add(responseData);
		}

		Collections.sort(jobResponseDatas,
				new Comparator<JobResponseData<S>>() {
					@Override
					public int compare(JobResponseData<S> o1,
							JobResponseData<S> o2) {
						return o1.getSlot() - o2.getSlot();
					}
				});

//		jobResponse = new JobResponse<>(jobId, jobResponseDatas);
	}

	public boolean isFinished() {
		return openTasksCount.compareAndSet(0, 0);
	}

	public void setJobState(JobState jobState) {
		synchronized (jobState) {
			this.jobState = jobState;
		}

		switch (jobState) {
		case NEW:
			for (JobHandler<T> jobHandler : jobHandlers) {
				jobHandler.onNew();
			}
			break;
		case SUBMITTED:
			for (JobHandler<T> jobHandler : jobHandlers) {
				jobHandler.onSubmitted();
			}
			break;
		case RUNNING:
			for (JobHandler<T> jobHandler : jobHandlers) {
				jobHandler.onRunning();
			}
			break;
		case FINISHED:
			// here we need to check if we have already called the onFinish()
			// handlers. by doing this with an AtomicBoolean, we don't have to
			// synchronize the calling code in addResult()
			if (!onFinishExecuted.getAndSet(true)) {
				for (JobHandler<T> jobHandler : jobHandlers) {
//					jobHandler.onFinished(jobId);
				}
			}
			break;
		case ERROR:
			for (JobHandler<T> jobHandler : jobHandlers) {
				jobHandler.onError();
			}
			break;
		default:
			break;
		}
	}

	public JobState getState() {
		return jobState;
	}

	public JobId getJobId() {
		return jobId;
	}

	public void setTaskState(TaskId taskId, TaskState taskState) {
		TaskRequest<T> taskRequest = taskRequests.get(taskId);
		taskRequest.setTaskState(taskState);
	}

	public JobResponse<S> getResult() {
		if (jobState != JobState.FINISHED) {
			LOG.error("!!!! Job {} should already be finished !!!! {}", jobId,
					Thread.currentThread());
			LOG.error("openTasksCount: {}", openTasksCount.get());
			LOG.error("jobState: {}", jobState);
			return null;
		}
		return jobResponse;
	}

}
