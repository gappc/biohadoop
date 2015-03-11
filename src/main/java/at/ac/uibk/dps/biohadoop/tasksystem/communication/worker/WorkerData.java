package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import at.ac.uibk.dps.biohadoop.tasksystem.Worker;

public class WorkerData<R, T, S> {

	private final Worker<R, T, S> worker;
	private final R initialData;

	public WorkerData(Worker<R, T, S> worker, R initialData) {
		this.worker = worker;
		this.initialData = initialData;
	}

	public Worker<R, T, S> getWorker() {
		return worker;
	}

	public R getInitialData() {
		return initialData;
	}
}
