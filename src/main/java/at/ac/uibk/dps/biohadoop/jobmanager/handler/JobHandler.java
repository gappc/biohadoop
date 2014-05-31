package at.ac.uibk.dps.biohadoop.jobmanager.handler;

import at.ac.uibk.dps.biohadoop.jobmanager.JobId;

public interface JobHandler<T> {
	public void onNew();
	public void onSubmitted();
	public void onRunning();
	public void onFinished(JobId jobId);
	public void onError();
}
