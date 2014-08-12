package at.ac.uibk.dps.biohadoop.communication;

import at.ac.uibk.dps.biohadoop.communication.worker.WorkerEndpoint;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkerConfiguration {

	private final Class<? extends WorkerEndpoint> worker;
	private final Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable;
	private final Integer count;

	public WorkerConfiguration(Class<? extends WorkerEndpoint> worker,
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			Integer count) {
		this.worker = worker;
		this.remoteExecutable = remoteExecutable;
		this.count = count;
	}

	@JsonCreator
	public static WorkerConfiguration create(
			@JsonProperty("worker") Class<? extends WorkerEndpoint> worker,
			@JsonProperty("remoteExecutable") Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			@JsonProperty("count") Integer count) {
		return new WorkerConfiguration(worker, remoteExecutable, count);
	}

	public Class<? extends WorkerEndpoint> getWorker() {
		return worker;
	}

	public Class<? extends RemoteExecutable<?, ?, ?>> getRemoteExecutable() {
		return remoteExecutable;
	}

	public Integer getCount() {
		return count;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WorkerEndpoint=").append(worker.getCanonicalName());
		sb.append(" RemoteExecutable=").append(
				remoteExecutable.getCanonicalName());
		sb.append(" Count=").append(count);
		return sb.toString();
	}

}
