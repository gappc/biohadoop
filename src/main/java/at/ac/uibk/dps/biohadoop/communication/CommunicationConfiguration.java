package at.ac.uibk.dps.biohadoop.communication;

import java.util.List;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.master.Master;
import at.ac.uibk.dps.biohadoop.communication.worker.Worker;
import at.ac.uibk.dps.biohadoop.utils.ClassAsKeyDeserializer;
import at.ac.uibk.dps.biohadoop.utils.ClassAsKeySerializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class CommunicationConfiguration {

	private final List<Class<? extends Master>> masters;
	// TODO: Check if can be improved; At the moment: key must be String because
	// of Json exception
	// com.fasterxml.jackson.databind.JsonMappingException: Can not find a (Map)
	// Key deserializer for type [simple type, class
	// java.lang.Class<at.ac.uibk.dps.biohadoop.connection.WorkerConnection>]
	@JsonSerialize(keyUsing = ClassAsKeySerializer.class)
	@JsonDeserialize(keyUsing = ClassAsKeyDeserializer.class)
	private final Map<Class<? extends Worker<?, ?>>, Integer> workers;

	public CommunicationConfiguration(
			List<Class<? extends Master>> masters,
			Map<Class<? extends Worker<?, ?>>, Integer> workers) {
		this.masters = masters;
		this.workers = workers;
	}

	@JsonCreator
	public static CommunicationConfiguration create(
			@JsonProperty("masters") List<Class<? extends Master>> masters,
			@JsonProperty("workers") Map<Class<? extends Worker<?, ?>>, Integer> workers) {
		return new CommunicationConfiguration(masters, workers);
	}

	public List<Class<? extends Master>> getMasters() {
		return masters;
	}

	public Map<Class<? extends Worker<?, ?>>, Integer> getWorkers() {
		return workers;
	}

}
