package at.ac.uibk.dps.biohadoop.tasksystem;

import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerConfiguration;

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
