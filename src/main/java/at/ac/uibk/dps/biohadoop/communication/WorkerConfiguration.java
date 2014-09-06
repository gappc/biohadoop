package at.ac.uibk.dps.biohadoop.communication;

import at.ac.uibk.dps.biohadoop.communication.worker.WorkerEndpoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkerConfiguration {

	private final Class<? extends WorkerEndpoint> worker;
	private final String settingName;
	private final Integer count;

	public WorkerConfiguration(Class<? extends WorkerEndpoint> worker,
			String settingName,
			Integer count) {
		this.worker = worker;
		this.settingName = settingName;
		this.count = count;
	}

	@JsonCreator
	public static WorkerConfiguration create(
			@JsonProperty("worker") Class<? extends WorkerEndpoint> worker,
			@JsonProperty("settingName") String settingName,
			@JsonProperty("count") Integer count) {
		return new WorkerConfiguration(worker, settingName, count);
	}

	public Class<? extends WorkerEndpoint> getWorker() {
		return worker;
	}

	public String getSettingName() {
		return settingName;
	}

	public Integer getCount() {
		return count;
	}

	@Override
	public String toString() {
		String workerClass = worker != null ? worker.getCanonicalName() : null;

		StringBuilder sb = new StringBuilder();
		sb.append("WorkerEndpoint=").append(workerClass);
		sb.append(" setting name=").append(settingName);
		sb.append(" Count=").append(count);
		return sb.toString();
	}

}
