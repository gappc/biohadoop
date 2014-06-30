package at.ac.uibk.dps.biohadoop.connection;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectionConfiguration {

	private final List<Class<? extends MasterConnection>> masterEndpoints;
	// TODO: Check if can be improved; At the moment: key must be String because
	// of Json exception
	// com.fasterxml.jackson.databind.JsonMappingException: Can not find a (Map)
	// Key deserializer for type [simple type, class
	// java.lang.Class<at.ac.uibk.dps.biohadoop.connection.WorkerConnection>]
	private final Map<String, Integer> workerEndpoints;

	public ConnectionConfiguration(
			List<Class<? extends MasterConnection>> masterEndpoints,
			Map<String, Integer> workerEndpoints) {
		this.masterEndpoints = masterEndpoints;
		this.workerEndpoints = workerEndpoints;
	}

	@JsonCreator
	public static ConnectionConfiguration create(
			@JsonProperty("masterEndpoints") List<Class<? extends MasterConnection>> masterEndpoints,
			@JsonProperty("workerEndpoints") Map<String, Integer> workerEndpoints) {
		return new ConnectionConfiguration(masterEndpoints, workerEndpoints);
	}

	public List<Class<? extends MasterConnection>> getMasterEndpoints() {
		return masterEndpoints;
	}

	public Map<String, Integer> getWorkerEndpoints() {
		return workerEndpoints;
	}
}
