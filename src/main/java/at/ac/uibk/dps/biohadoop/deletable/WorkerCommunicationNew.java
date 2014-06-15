package at.ac.uibk.dps.biohadoop.deletable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkerCommunicationNew {

	private final String worker;
	private final int count;

	public WorkerCommunicationNew(String worker, int count) {
		this.worker = worker;
		this.count = count;
	}

	@JsonCreator
	public static WorkerCommunicationNew create(
			@JsonProperty("worker") String worker,
			@JsonProperty("count") int count) {
		return new WorkerCommunicationNew(worker, count);
	}

	public String getWorker() {
		return worker;
	}

	public int getCount() {
		return count;
	}

}
