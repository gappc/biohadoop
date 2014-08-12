package at.ac.uibk.dps.biohadoop.communication;

import at.ac.uibk.dps.biohadoop.communication.worker.WorkerEndpoint;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkerConfiguration {

	private final Class<? extends WorkerEndpoint> worker;
	private final Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable;
	private final Class<?> annotation;
	private final Integer count;

	public WorkerConfiguration(Class<? extends WorkerEndpoint> worker,
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			Class<?> annotation, Integer count) {
		this.worker = worker;
		this.remoteExecutable = remoteExecutable;
		this.annotation = annotation;
		this.count = count;
	}

	@JsonCreator
	public static WorkerConfiguration create(
			@JsonProperty("worker") Class<? extends WorkerEndpoint> worker,
			@JsonProperty("remoteExecutable") Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			@JsonProperty("annotation") Class<?> annotation,
			@JsonProperty("count") Integer count) {
		return new WorkerConfiguration(worker, remoteExecutable, annotation,
				count);
	}

	public Class<? extends WorkerEndpoint> getWorker() {
		return worker;
	}

	public Class<? extends RemoteExecutable<?, ?, ?>> getRemoteExecutable() {
		return remoteExecutable;
	}

	public Class<?> getAnnotation() {
		return annotation;
	}

	public Integer getCount() {
		return count;
	}

}
