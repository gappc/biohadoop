package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import at.ac.uibk.dps.biohadoop.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.AdapterConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.KryoAdapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.WebSocketAdapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.Worker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerConfiguration;

public class BiohadoopConfiguration {

	public static final String CONFIG_PATH = "CONFIG_PATH";

	private final List<String> includePaths;
	private final List<SolverConfiguration> solverConfigurations;
	private final CommunicationConfiguration communicationConfiguration;
	private final Map<String, String> globalProperties;

	public BiohadoopConfiguration(List<String> includePaths,
			List<SolverConfiguration> solverConfigurations,
			CommunicationConfiguration communicationConfiguration,
			Map<String, String> globalProperties) {
		this.includePaths = includePaths;
		this.solverConfigurations = solverConfigurations;
		this.communicationConfiguration = communicationConfiguration;
		this.globalProperties = globalProperties;
	}

	@JsonCreator
	public static BiohadoopConfiguration create(
			@JsonProperty("includePaths") List<String> includePaths,
			@JsonProperty("solverConfiguration") List<SolverConfiguration> solverConfiguration,
			@JsonProperty("communicationConfiguration") CommunicationConfiguration communicationConfiguration,
			@JsonProperty("globalProperties") Map<String, String> globalProperties) {
		return new BiohadoopConfiguration(includePaths, solverConfiguration,
				communicationConfiguration, globalProperties);
	}

	public List<String> getIncludePaths() {
		return includePaths;
	}

	public List<SolverConfiguration> getSolverConfigurations() {
		return solverConfigurations;
	}

	public CommunicationConfiguration getCommunicationConfiguration() {
		return communicationConfiguration;
	}

	public Map<String, String> getGlobalProperties() {
		return globalProperties;
	}

	public static class Builder {
		private List<String> libPaths = new ArrayList<>();
		private List<SolverConfiguration> solverConfigurations = new ArrayList<>();;
		private List<AdapterConfiguration> adapters = new ArrayList<>();
		private List<WorkerConfiguration> workerConfigurations = new ArrayList<>();
		private Map<String, String> globalProperties = new HashMap<>();

		public Builder addLibPath(String path) {
			libPaths.add(path);
			return this;
		}

		public Builder addSolver(SolverConfiguration solverConfiguration) {
			solverConfigurations.add(solverConfiguration);
			return this;
		}

		public Builder addDefaultAdapters() {
			adapters.add(new AdapterConfiguration(KryoAdapter.class));
			adapters.add(new AdapterConfiguration(WebSocketAdapter.class));
			return this;
		}
		
		public Builder addAdapter(
				Class<? extends Adapter> adapter) {
			AdapterConfiguration adapterConfiguration = new AdapterConfiguration(
					adapter);
			adapters.add(adapterConfiguration);
			return this;
		}

		public Builder addWorker(Class<? extends Worker> worker, int count) {
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
					adapters, workerConfigurations);
			return new BiohadoopConfiguration(libPaths, solverConfigurations,
					communicationConfiguration, globalProperties);
		}

	}

}
