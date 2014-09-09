package at.ac.uibk.dps.biohadoop.communication;

import java.util.List;

import at.ac.uibk.dps.biohadoop.communication.adapter.AdapterConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommunicationConfiguration {

	private final List<AdapterConfiguration> dedicatedAdapters;
	private final List<WorkerConfiguration> workerConfigurations;

	public CommunicationConfiguration(
			List<AdapterConfiguration> dedicatedAdapters,
			List<WorkerConfiguration> workerConfigurations) {
		this.dedicatedAdapters = dedicatedAdapters;
		this.workerConfigurations = workerConfigurations;
	}

	@JsonCreator
	public static CommunicationConfiguration create(
			@JsonProperty("dedicatedAdapters") List<AdapterConfiguration> dedicatedAdapters,
			@JsonProperty("workerConfigurations") List<WorkerConfiguration> workerConfigurations) {
		return new CommunicationConfiguration(dedicatedAdapters, workerConfigurations);
	}

	public List<AdapterConfiguration> getDedicatedAdapters() {
		return dedicatedAdapters;
	}

	public List<WorkerConfiguration> getWorkerConfigurations() {
		return workerConfigurations;
	}

}
