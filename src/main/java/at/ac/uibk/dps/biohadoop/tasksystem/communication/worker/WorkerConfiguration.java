package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class WorkerConfiguration {

	private final Class<? extends WorkerComm> worker;
	private final Integer count;

	public WorkerConfiguration(Class<? extends WorkerComm> worker, Integer count) {
		this.worker = worker;
		this.count = count;
	}

	@JsonCreator
	public static WorkerConfiguration create(
			@JsonProperty("worker") Class<? extends WorkerComm> worker,
			@JsonProperty("count") Integer count) {
		return new WorkerConfiguration(worker, count);
	}

	public Class<? extends WorkerComm> getWorker() {
		return worker;
	}

	public Integer getCount() {
		return count;
	}

	@Override
	public String toString() {
		String workerClassname = worker != null ? worker.getCanonicalName()
				: null;

		StringBuilder sb = new StringBuilder();
		sb.append("worker=").append(workerClassname);
		sb.append(" count=").append(count);
		return sb.toString();
	}

}
