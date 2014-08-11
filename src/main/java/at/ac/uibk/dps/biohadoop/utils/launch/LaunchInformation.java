package at.ac.uibk.dps.biohadoop.utils.launch;

import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

public class LaunchInformation {

	private final Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable;
	private final MasterLifecycle master;
	private final String queueName;

	public LaunchInformation(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			MasterLifecycle master, String queueName) {
		this.remoteExecutable = remoteExecutable;
		this.master = master;
		this.queueName = queueName;
	}

	public Class<? extends RemoteExecutable<?, ?, ?>> getRemoteExecutable() {
		return remoteExecutable;
	}

	public MasterLifecycle getMaster() {
		return master;
	}

	public String getQueueName() {
		return queueName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RemoteExecutable: ")
				.append(remoteExecutable.getCanonicalName()).append("Class=")
				.append(master.getClass().getCanonicalName())
				.append(", queueName=").append(queueName);
		return sb.toString();
	}

}
