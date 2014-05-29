package at.ac.uibk.dps.biohadoop.jobmanager.handler;

import at.ac.uibk.dps.biohadoop.jobmanager.api.JobResponse;

public interface JobHandler<T> {
	public void onNew();
	public void onSubmitted();
	public void onRunning();
	public void onFinished(JobResponse<T> jobResponse);
	public void onError();
}
