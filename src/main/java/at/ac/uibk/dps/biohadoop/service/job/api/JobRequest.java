package at.ac.uibk.dps.biohadoop.service.job.api;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.service.job.handler.JobHandler;
import at.ac.uibk.dps.biohadoop.service.job.handler.SimpleJobHandler;

public class JobRequest<T> {

	private List<JobRequestData<T>> requestList = new ArrayList<>();
	private final JobHandler<T> handler;

	public JobRequest() {
		handler = new SimpleJobHandler<T>();
	}

	public JobRequest(JobHandler<T> handler) {
		this.handler = handler;
	}

	public void add(final JobRequestData<T> requestData) {
		requestList.add(requestData);
	}

	public List<JobRequestData<T>> getRequestData() {
		return requestList;
	}

	public JobHandler<T> getHandler() {
		return handler;
	}
}