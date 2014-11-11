package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.EndpointConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerConfiguration;

public class CommunicationConfiguration {

	private final List<EndpointConfiguration> endpoints;
	private final List<WorkerConfiguration> workers;

	public CommunicationConfiguration(
			List<EndpointConfiguration> endpoints,
			List<WorkerConfiguration> workers) {
		this.endpoints = endpoints;
		this.workers = workers;
	}

	@JsonCreator
	public static CommunicationConfiguration create(
			@JsonProperty("endpoints") List<EndpointConfiguration> endpoints,
			@JsonProperty("workers") List<WorkerConfiguration> workers) {
		return new CommunicationConfiguration(endpoints, workers);
	}

	public List<EndpointConfiguration> getEndpoints() {
		return endpoints;
	}

	public List<WorkerConfiguration> getWorkers() {
		return workers;
	}

}
