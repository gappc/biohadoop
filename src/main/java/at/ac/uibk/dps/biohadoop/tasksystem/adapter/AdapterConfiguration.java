package at.ac.uibk.dps.biohadoop.tasksystem.adapter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AdapterConfiguration {

	private final Class<? extends Adapter> adapter;
	private String settingName;

	public AdapterConfiguration(Class<? extends Adapter> adapter,
			String settingName) {
		this.adapter = adapter;
		this.settingName = settingName;
	}

	@JsonCreator
	public static AdapterConfiguration create(
			@JsonProperty("adapter") Class<? extends Adapter> adapter,
			@JsonProperty("settingName") String settingName) {
		return new AdapterConfiguration(adapter, settingName);
	}

	public Class<? extends Adapter> getAdapter() {
		return adapter;
	}

	public String getSettingName() {
		return settingName;
	}

	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}

	@Override
	public String toString() {
		String adapterClass = adapter != null ? adapter.getCanonicalName() : null;

		StringBuilder sb = new StringBuilder();
		sb.append("adapter=").append(adapterClass);
		sb.append(" setting name=").append(settingName);
		return sb.toString();
	}

}
