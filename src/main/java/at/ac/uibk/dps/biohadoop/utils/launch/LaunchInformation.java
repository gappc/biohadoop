package at.ac.uibk.dps.biohadoop.utils.launch;

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;

public class LaunchInformation {

	private final Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable;
	private final MasterEndpoint master;
	private final String settingName;

	// TODO sort parameters to the following order: master, remoteExecutable,
	// settingName
	public LaunchInformation(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			MasterEndpoint master, String settingName) {
		this.remoteExecutable = remoteExecutable;
		this.master = master;
		this.settingName = settingName;
	}

	public Class<? extends RemoteExecutable<?, ?, ?>> getRemoteExecutable() {
		return remoteExecutable;
	}

	public MasterEndpoint getMaster() {
		return master;
	}

	public String getSettingName() {
		return settingName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RemoteExecutable=")
				.append(remoteExecutable.getCanonicalName())
				.append(", MasterEndpoint=")
				.append(master.getClass().getCanonicalName())
				.append(", setting name=").append(settingName);
		return sb.toString();
	}

}
