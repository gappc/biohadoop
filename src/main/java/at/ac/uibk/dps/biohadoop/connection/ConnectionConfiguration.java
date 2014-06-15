package at.ac.uibk.dps.biohadoop.connection;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConnectionConfiguration {

	private final List<FileMasterConfiguration> masters;
	// TODO: Check if can be improved; At the moment: key must be String because
	// of Json exception
	// com.fasterxml.jackson.databind.JsonMappingException: Can not find a (Map)
	// Key deserializer for type [simple type, class
	// java.lang.Class<at.ac.uibk.dps.biohadoop.connection.WorkerConnection>]
	private final Map<String, Integer> workers;

	public ConnectionConfiguration(List<FileMasterConfiguration> masters,
			Map<String, Integer> workers) {
		this.masters = masters;
		this.workers = workers;
	}

	@JsonCreator
	public static ConnectionConfiguration create(
			@JsonProperty("masters") List<FileMasterConfiguration> masters,
			@JsonProperty("workers") Map<String, Integer> workers) {
		return new ConnectionConfiguration(masters, workers);
	}

	public List<FileMasterConfiguration> getMasters() {
		return masters;
	}

	public Map<String, Integer> getWorkers() {
		return workers;
	}
}
