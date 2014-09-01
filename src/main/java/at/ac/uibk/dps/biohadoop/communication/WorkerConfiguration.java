package at.ac.uibk.dps.biohadoop.communication;

import at.ac.uibk.dps.biohadoop.communication.worker.WorkerEndpoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkerConfiguration {

	private final Class<? extends WorkerEndpoint> worker;
	private final String queueName;
	private final Integer count;

	public WorkerConfiguration(Class<? extends WorkerEndpoint> worker,
			String queueName,
			Integer count) {
		this.worker = worker;
		this.queueName = queueName;
		this.count = count;
	}

	@JsonCreator
	public static WorkerConfiguration create(
			@JsonProperty("worker") Class<? extends WorkerEndpoint> worker,
			@JsonProperty("queueName") String queueName,
			@JsonProperty("count") Integer count) {
		return new WorkerConfiguration(worker, queueName, count);
	}

	public Class<? extends WorkerEndpoint> getWorker() {
		return worker;
	}

	public String getQueueName() {
		return queueName;
	}

	public Integer getCount() {
		return count;
	}

	@Override
	public String toString() {
		String workerClass = worker != null ? worker.getCanonicalName() : null;

		StringBuilder sb = new StringBuilder();
		sb.append("WorkerEndpoint=").append(workerClass);
		sb.append(" QueueName=").append(queueName);
		sb.append(" Count=").append(count);
		return sb.toString();
	}

}
