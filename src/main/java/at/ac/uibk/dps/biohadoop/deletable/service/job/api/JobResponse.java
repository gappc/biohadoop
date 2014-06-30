package at.ac.uibk.dps.biohadoop.deletable.service.job.api;

import java.util.Collections;
import java.util.List;

import at.ac.uibk.dps.biohadoop.deletable.JobId;

public class JobResponse<T> {

	private final JobId jobId;
	private final List<JobResponseData<T>> responses;

	public JobResponse(final JobId jobId, final List<JobResponseData<T>> responses) {
		this.jobId = jobId;
		this.responses = responses;
	}

	public List<JobResponseData<T>> getResponseData() {
		return Collections.unmodifiableList(responses);
	}

	public JobId getJobId() {
		return jobId;
	}
}
