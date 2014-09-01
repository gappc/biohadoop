package at.ac.uibk.dps.biohadoop.communication;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MasterConfiguration {

	private final Class<? extends MasterEndpoint> master;
	private final Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable;
	private String queueName;

	public MasterConfiguration(Class<? extends MasterEndpoint> master,
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			String queueName) {
		this.master = master;
		this.remoteExecutable = remoteExecutable;
		this.queueName = queueName;
	}

	@JsonCreator
	public static MasterConfiguration create(
			@JsonProperty("master") Class<? extends MasterEndpoint> master,
			@JsonProperty("remoteExecutable") Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			@JsonProperty("queueName") String queueName) {
		return new MasterConfiguration(master, remoteExecutable, queueName);
	}

	public Class<? extends MasterEndpoint> getMaster() {
		return master;
	}

	public Class<? extends RemoteExecutable<?, ?, ?>> getRemoteExecutable() {
		return remoteExecutable;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	@Override
	public String toString() {
		String masterClass = master != null ? master.getCanonicalName() : null;
		String remoteExecutableClass = remoteExecutable != null ? remoteExecutable
				.getCanonicalName() : null;

		StringBuilder sb = new StringBuilder();
		sb.append("MasterEndpoint=").append(masterClass);
		sb.append(" RemoteExecutable=").append(remoteExecutableClass);
		sb.append(" queueName=").append(queueName);
		return sb.toString();
	}

}
