package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.AdapterConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerConfiguration;

public class CommunicationConfiguration {

	private final List<AdapterConfiguration> adapters;
	private final List<WorkerConfiguration> workers;

	public CommunicationConfiguration(
			List<AdapterConfiguration> adapters,
			List<WorkerConfiguration> workers) {
		this.adapters = adapters;
		this.workers = workers;
	}

	@JsonCreator
	public static CommunicationConfiguration create(
			@JsonProperty("adapters") List<AdapterConfiguration> adapters,
			@JsonProperty("workers") List<WorkerConfiguration> workers) {
		return new CommunicationConfiguration(adapters, workers);
	}

	public List<AdapterConfiguration> getAdapters() {
		return adapters;
	}

	public List<WorkerConfiguration> getWorkers() {
		return workers;
	}

}
