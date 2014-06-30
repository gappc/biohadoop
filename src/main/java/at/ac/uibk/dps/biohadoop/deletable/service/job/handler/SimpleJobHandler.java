package at.ac.uibk.dps.biohadoop.deletable.service.job.handler;

import at.ac.uibk.dps.biohadoop.deletable.JobId;

public class SimpleJobHandler<T> implements JobHandler<T> {

	@Override
	public void onNew() {
	}

	@Override
	public void onSubmitted() {
	}

	@Override
	public void onRunning() {
	}

	@Override
	public void onFinished(JobId jobId) {
	}

	@Override
	public void onError() {
	}

}
