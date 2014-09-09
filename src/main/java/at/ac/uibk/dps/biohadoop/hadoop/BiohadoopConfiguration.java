package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.communication.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.communication.adapter.AdapterConfiguration;
import at.ac.uibk.dps.biohadoop.communication.worker.Worker;
import at.ac.uibk.dps.biohadoop.queue.SimpleTaskSubmitter;
import at.ac.uibk.dps.biohadoop.solver.SolverConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
		private List<AdapterConfiguration> dedicatedAdapters = new ArrayList<>();
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

		public Builder addDedicatedAdapter(
				Class<? extends Adapter> dedicatedAdapter,
				String settingName) {
			AdapterConfiguration adapterConfiguration = new AdapterConfiguration(
					dedicatedAdapter, settingName);
			dedicatedAdapters.add(adapterConfiguration);
			return this;
		}

		public Builder addWorker(Class<? extends Worker> worker,
				int count) {
			addDedicatedWorker(worker, SimpleTaskSubmitter.SETTING_NAME, count);
			return this;
		}

		public Builder addDedicatedWorker(
				Class<? extends Worker> worker,
				String settingName,
				int count) {
			WorkerConfiguration workerConfiguration = new WorkerConfiguration(
					worker, settingName, count);
			workerConfigurations.add(workerConfiguration);
			return this;
		}

		public Builder addGobalProperty(String key, String value) {
			globalProperties.put(key, value);
			return this;
		}

		public BiohadoopConfiguration build() {
			CommunicationConfiguration communicationConfiguration = new CommunicationConfiguration(
					dedicatedAdapters, workerConfigurations);
			return new BiohadoopConfiguration(libPaths, solverConfigurations,
					communicationConfiguration, globalProperties);
		}

	}

}
