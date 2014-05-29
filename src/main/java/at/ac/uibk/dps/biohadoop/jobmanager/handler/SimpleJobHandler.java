package at.ac.uibk.dps.biohadoop.jobmanager.handler;

import at.ac.uibk.dps.biohadoop.jobmanager.api.JobResponse;

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
	public void onFinished(JobResponse<T> jobResponse) {
	}

	@Override
	public void onError() {
	}

}
