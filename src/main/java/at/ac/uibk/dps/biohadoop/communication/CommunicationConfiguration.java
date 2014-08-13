package at.ac.uibk.dps.biohadoop.communication;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommunicationConfiguration {

	private final List<MasterConfiguration> dedicatedMasters;
	private final List<WorkerConfiguration> workerConfigurations;

	public CommunicationConfiguration(
			List<MasterConfiguration> dedicatedMasters,
			List<WorkerConfiguration> workerConfigurations) {
		this.dedicatedMasters = dedicatedMasters;
		this.workerConfigurations = workerConfigurations;
	}

	@JsonCreator
	public static CommunicationConfiguration create(
			@JsonProperty("dedicatedMasters") List<MasterConfiguration> dedicatedMasters,
			@JsonProperty("workerConfigurations") List<WorkerConfiguration> workerConfigurations) {
		return new CommunicationConfiguration(dedicatedMasters, workerConfigurations);
	}

	public List<MasterConfiguration> getDedicatedMasters() {
		return dedicatedMasters;
	}

	public List<WorkerConfiguration> getWorkerConfigurations() {
		return workerConfigurations;
	}

}
