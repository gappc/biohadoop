package at.ac.uibk.dps.biohadoop.jobmanager.api;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.jobmanager.JobId;

public class JobResponse<T> {

	private final JobId jobId;
	private List<JobResponseData<T>> responses = new ArrayList<>();

	public JobResponse(JobId jobId) {
		this.jobId = jobId;
	}

	public void add(final JobResponseData<T> response) {
		responses.add(response);
	}

	public List<JobResponseData<T>> getResponseData() {
		return responses;
	}

	public JobId getJobId() {
		return jobId;
	}
}
