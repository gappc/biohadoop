package at.ac.uibk.dps.biohadoop.communication;

import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.master.rest2.SuperComputable;
import at.ac.uibk.dps.biohadoop.communication.worker.SuperWorker;
import at.ac.uibk.dps.biohadoop.utils.ClassAsKeyDeserializer;
import at.ac.uibk.dps.biohadoop.utils.ClassAsKeySerializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class CommunicationConfiguration {

	private final List<Class<? extends MasterLifecycle>> masterEndpoints;
	private final List<Class<? extends SuperComputable>> masters;
	// TODO: Check if can be improved; At the moment: key must be String because
	// of Json exception
	// com.fasterxml.jackson.databind.JsonMappingException: Can not find a (Map)
	// Key deserializer for type [simple type, class
	// java.lang.Class<at.ac.uibk.dps.biohadoop.connection.WorkerConnection>]
	private final Map<String, Integer> workerEndpoints;
	@JsonSerialize(keyUsing=ClassAsKeySerializer.class)
	@JsonDeserialize(keyUsing=ClassAsKeyDeserializer.class)
	private final Map<Class<? extends SuperWorker<?, ?>>, Integer> workers;

	public CommunicationConfiguration(
			List<Class<? extends MasterLifecycle>> masterEndpoints, List<Class<? extends SuperComputable>> masters,
			Map<String, Integer> workerEndpoints, Map<Class<? extends SuperWorker<?, ?>>, Integer> workers) {
		this.masterEndpoints = masterEndpoints;
		this.masters = masters;
		this.workerEndpoints = workerEndpoints;
		this.workers = workers;
	}

	@JsonCreator
	public static CommunicationConfiguration create(
			@JsonProperty("masterEndpoints") List<Class<? extends MasterLifecycle>> masterEndpoints,
			@JsonProperty("masters") List<Class<? extends SuperComputable>> masters,
			@JsonProperty("workerEndpoints") Map<String, Integer> workerEndpoints,
			@JsonProperty("workers") Map<Class<? extends SuperWorker<?, ?>>, Integer> workers) {
		return new CommunicationConfiguration(masterEndpoints, masters, workerEndpoints, workers);
	}

	public List<Class<? extends MasterLifecycle>> getMasterEndpoints() {
		return masterEndpoints;
	}
	
	public List<Class<? extends SuperComputable>> getMasters() {
		return masters;
	}

	public Map<String, Integer> getWorkerEndpoints() {
		return workerEndpoints;
	}

	public Map<Class<? extends SuperWorker<?, ?>>, Integer> getWorkers() {
		return workers;
	}
	
}
