package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.EndpointConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.KryoEndpoint;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.WebSocketEndpoint;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerComm;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerConfiguration;

public class BiohadoopConfiguration {

	public static final String CONFIG_PATH = "CONFIG_PATH";

	private final List<String> includePaths;
	private final List<AlgorithmConfiguration> algorithmConfigurations;
	private final CommunicationConfiguration communicationConfiguration;
	private final Map<String, String> globalProperties;

	public BiohadoopConfiguration(List<String> includePaths,
			List<AlgorithmConfiguration> algorithmConfigurations,
			CommunicationConfiguration communicationConfiguration,
			Map<String, String> globalProperties) {
		this.includePaths = includePaths;
		this.algorithmConfigurations = algorithmConfigurations;
		this.communicationConfiguration = communicationConfiguration;
		this.globalProperties = globalProperties;
	}

	@JsonCreator
	public static BiohadoopConfiguration create(
			@JsonProperty("includePaths") List<String> includePaths,
			@JsonProperty("algorithmConfiguration") List<AlgorithmConfiguration> algorithmConfiguration,
			@JsonProperty("communicationConfiguration") CommunicationConfiguration communicationConfiguration,
			@JsonProperty("globalProperties") Map<String, String> globalProperties) {
		return new BiohadoopConfiguration(includePaths, algorithmConfiguration,
				communicationConfiguration, globalProperties);
	}

	public List<String> getIncludePaths() {
		return includePaths;
	}

	public List<AlgorithmConfiguration> getAlgorithmConfigurations() {
		return algorithmConfigurations;
	}

	public CommunicationConfiguration getCommunicationConfiguration() {
		return communicationConfiguration;
	}

	public Map<String, String> getGlobalProperties() {
		return globalProperties;
	}

	public static class Builder {
		private List<String> libPaths = new ArrayList<>();
		private List<AlgorithmConfiguration> algorithmConfigurations = new ArrayList<>();;
		private List<EndpointConfiguration> endpoints = new ArrayList<>();
		private List<WorkerConfiguration> workerConfigurations = new ArrayList<>();
		private Map<String, String> globalProperties = new HashMap<>();

		public Builder addLibPath(String path) {
			libPaths.add(path);
			return this;
		}

		public Builder addAlgorithm(AlgorithmConfiguration algorithmConfiguration) {
			algorithmConfigurations.add(algorithmConfiguration);
			return this;
		}

		public Builder addDefaultEndpoints() {
			endpoints.add(new EndpointConfiguration(KryoEndpoint.class));
			endpoints.add(new EndpointConfiguration(WebSocketEndpoint.class));
			return this;
		}
		
		public Builder addEndpoint(
				Class<? extends Endpoint> endpoint) {
			EndpointConfiguration endpointConfiguration = new EndpointConfiguration(
					endpoint);
			endpoints.add(endpointConfiguration);
			return this;
		}

		public Builder addWorker(Class<? extends WorkerComm> worker, int count) {
			WorkerConfiguration workerConfiguration = new WorkerConfiguration(
					worker, count);
			workerConfigurations.add(workerConfiguration);
			return this;
		}

		public Builder addGobalProperty(String key, String value) {
			globalProperties.put(key, value);
			return this;
		}

		public BiohadoopConfiguration build() {
			CommunicationConfiguration communicationConfiguration = new CommunicationConfiguration(
					endpoints, workerConfigurations);
			return new BiohadoopConfiguration(libPaths, algorithmConfigurations,
					communicationConfiguration, globalProperties);
		}

	}

}
