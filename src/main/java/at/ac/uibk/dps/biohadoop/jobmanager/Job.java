package at.ac.uibk.dps.biohadoop.jobmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import at.ac.uibk.dps.biohadoop.jobmanager.api.JobRequest;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobRequestData;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobResponse;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobResponseData;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobState;
import at.ac.uibk.dps.biohadoop.jobmanager.handler.JobHandler;

public class Job<T, S> {

	private final JobId jobId;
	private AtomicInteger openTasksCount = new AtomicInteger();
	private JobState jobState = JobState.NEW;
	private Map<TaskId, TaskRequest<T>> taskRequests = new ConcurrentHashMap<>();
	private Map<TaskId, TaskResponse<S>> taskResponses = new ConcurrentHashMap<>();
	private List<JobHandler<T>> jobHandlers = new ArrayList<>();
	private JobResponse<S> jobResponse;
	
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
			setJobState(JobState.FINISHED);
		}
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
			for (JobHandler<T> jobHandler : jobHandlers) {
				jobHandler.onFinished(jobId);
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
			for (TaskId taskId : taskRequests.keySet()) {
				TaskRequest<T> taskRequest = taskRequests.get(taskId);
				System.out.println(taskRequest);
			}
			System.out.println("openTasksCount: " + openTasksCount.get());
			System.out.println("jobState: " + jobState);
			return null;
		}
		if (jobResponse == null) {
			List<JobResponseData<S>> jobResponseDatas = new ArrayList<>();
			
			for (TaskId taskId : taskResponses.keySet()) {
				TaskResponse<S> taskResponse = taskResponses.get(taskId);
				S data = taskResponse.getData().getData();
				int slot = taskResponse.getSlot();
				JobResponseData<S> responseData = new JobResponseData<S>(data, slot);
				jobResponseDatas.add(responseData);
			}
			
			Collections.sort(jobResponseDatas, new Comparator<JobResponseData<S>>() {
				@Override
				public int compare(JobResponseData<S> o1, JobResponseData<S> o2) {
					return o1.getSlot() - o2.getSlot();
				}
			});
			
			jobResponse = new JobResponse<>(jobId, jobResponseDatas);
		}
		return jobResponse;
	}

}
