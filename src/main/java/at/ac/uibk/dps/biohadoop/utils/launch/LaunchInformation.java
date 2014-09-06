package at.ac.uibk.dps.biohadoop.utils.launch;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;

public class LaunchInformation {

	private final MasterEndpoint master;
	private final String settingName;

	public LaunchInformation(MasterEndpoint master, String settingName) {
		this.master = master;
		this.settingName = settingName;
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
		sb.append(", MasterEndpoint=")
				.append(master.getClass().getCanonicalName())
				.append(", setting name=").append(settingName);
		return sb.toString();
	}

}
