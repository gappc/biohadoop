package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import at.ac.uibk.dps.biohadoop.tasksystem.AsyncComputable;

public class WorkerData<R, T, S> {

	private final AsyncComputable<R, T, S> asyncComputable;
	private final R initialData;

	public WorkerData(AsyncComputable<R, T, S> asyncComputable, R initialData) {
		this.asyncComputable = asyncComputable;
		this.initialData = initialData;
	}

	public AsyncComputable<R, T, S> getAsyncComputable() {
		return asyncComputable;
	}

	public R getInitialData() {
		return initialData;
	}
}
