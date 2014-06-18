package at.ac.uibk.dps.biohadoop.service.job.handler;

import at.ac.uibk.dps.biohadoop.service.job.JobId;

public interface JobHandler<T> {
	public void onNew();
	public void onSubmitted();
	public void onRunning();
	public void onFinished(JobId jobId);
	public void onError();
}
