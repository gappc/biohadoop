package at.ac.uibk.dps.biohadoop.utils.launch;

import at.ac.uibk.dps.biohadoop.tasksystem.adapter.Adapter;

public class LaunchInformation {

	private final Adapter adapter;
	private final String settingName;

	public LaunchInformation(Adapter adapter, String settingName) {
		this.adapter = adapter;
		this.settingName = settingName;
	}

	public Adapter getAdapter() {
		return adapter;
	}

	public String getSettingName() {
		return settingName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(", adapter=")
				.append(adapter.getClass().getCanonicalName())
				.append(", setting name=").append(settingName);
		return sb.toString();
	}

}
