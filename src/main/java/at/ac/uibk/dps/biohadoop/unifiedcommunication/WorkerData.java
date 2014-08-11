package at.ac.uibk.dps.biohadoop.unifiedcommunication;

public class WorkerData<R, T, S> {

	private final RemoteExecutable<R, T, S> remoteExecutable;
	private final R initialData;

	public WorkerData(RemoteExecutable<R, T, S> remoteExecutable,
			R initialData) {
		this.remoteExecutable = remoteExecutable;
		this.initialData = initialData;
	}

	public RemoteExecutable<R, T, S> getRemoteExecutable() {
		return remoteExecutable;
	}

	public R getInitialData() {
		return initialData;
	}
}
