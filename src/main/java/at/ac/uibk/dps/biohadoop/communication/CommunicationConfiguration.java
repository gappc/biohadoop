package at.ac.uibk.dps.biohadoop.communication;

import java.util.List;

import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CommunicationConfiguration {

	private final List<Class<? extends RemoteExecutable<?, ?, ?>>> masters;
//	@JsonSerialize(keyUsing = ClassAsKeySerializer.class)
//	@JsonDeserialize(keyUsing = ClassAsKeyDeserializer.class)
	private final List<WorkerConfiguration> workerConfigurations;

	public CommunicationConfiguration(
			List<Class<? extends RemoteExecutable<?, ?, ?>>> masters,
			List<WorkerConfiguration> workerConfigurations) {
		this.masters = masters;
		this.workerConfigurations = workerConfigurations;
	}

	@JsonCreator
	public static CommunicationConfiguration create(
			@JsonProperty("masters") List<Class<? extends RemoteExecutable<?, ?, ?>>> masters,
			@JsonProperty("workerConfigurations") List<WorkerConfiguration> workerConfigurations) {
		return new CommunicationConfiguration(masters, workerConfigurations);
	}

	public List<Class<? extends RemoteExecutable<?, ?, ?>>> getMasters() {
		return masters;
	}

	public List<WorkerConfiguration> getWorkerConfigurations() {
		return workerConfigurations;
	}

}
