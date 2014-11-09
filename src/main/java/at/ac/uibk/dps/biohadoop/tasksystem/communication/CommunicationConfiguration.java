package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.AdapterConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerConfiguration;

public class CommunicationConfiguration {

	private final List<AdapterConfiguration> adapters;
	private final List<WorkerConfiguration> workerConfigurations;

	public CommunicationConfiguration(
			List<AdapterConfiguration> adapters,
			List<WorkerConfiguration> workerConfigurations) {
		this.adapters = adapters;
		this.workerConfigurations = workerConfigurations;
	}

	@JsonCreator
	public static CommunicationConfiguration create(
			@JsonProperty("adapters") List<AdapterConfiguration> adapters,
			@JsonProperty("workerConfigurations") List<WorkerConfiguration> workerConfigurations) {
		return new CommunicationConfiguration(adapters, workerConfigurations);
	}

	public List<AdapterConfiguration> getAdapters() {
		return adapters;
	}

	public List<WorkerConfiguration> getWorkerConfigurations() {
		return workerConfigurations;
	}

}
