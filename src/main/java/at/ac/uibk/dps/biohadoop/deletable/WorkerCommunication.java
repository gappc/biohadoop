package at.ac.uibk.dps.biohadoop.deletable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkerCommunication {

	private final ConnectionType communicationType;
	private final int count;

	public WorkerCommunication(ConnectionType communicationType, int count) {
		this.communicationType = communicationType;
		this.count = count;
	}

	@JsonCreator
	public static WorkerCommunication create(
			@JsonProperty("communicationType") ConnectionType communicationType,
			@JsonProperty("count") int count) {
		return new WorkerCommunication(communicationType, count);
	}

	public ConnectionType getCommunicationType() {
		return communicationType;
	}

	public int getCount() {
		return count;
	}

}
