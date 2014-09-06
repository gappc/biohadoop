package at.ac.uibk.dps.biohadoop.communication;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MasterConfiguration {

	private final Class<? extends MasterEndpoint> master;
	private String settingName;

	public MasterConfiguration(Class<? extends MasterEndpoint> master,
			String settingName) {
		this.master = master;
		this.settingName = settingName;
	}

	@JsonCreator
	public static MasterConfiguration create(
			@JsonProperty("master") Class<? extends MasterEndpoint> master,
			@JsonProperty("settingName") String settingName) {
		return new MasterConfiguration(master, settingName);
	}

	public Class<? extends MasterEndpoint> getMaster() {
		return master;
	}

	public String getSettingName() {
		return settingName;
	}

	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}

	@Override
	public String toString() {
		String masterClass = master != null ? master.getCanonicalName() : null;

		StringBuilder sb = new StringBuilder();
		sb.append("MasterEndpoint=").append(masterClass);
		sb.append(" setting name=").append(settingName);
		return sb.toString();
	}

}
