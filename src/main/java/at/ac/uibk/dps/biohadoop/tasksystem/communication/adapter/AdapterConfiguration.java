package at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class AdapterConfiguration {

	private final Class<? extends Adapter> adapter;

	public AdapterConfiguration(Class<? extends Adapter> adapter) {
		this.adapter = adapter;
	}

	@JsonCreator
	public static AdapterConfiguration create(
			@JsonProperty("adapter") Class<? extends Adapter> adapter) {
		return new AdapterConfiguration(adapter);
	}

	public Class<? extends Adapter> getAdapter() {
		return adapter;
	}

	@Override
	public String toString() {
		String adapterClass = adapter != null ? adapter.getCanonicalName() : null;

		StringBuilder sb = new StringBuilder();
		sb.append("adapter=").append(adapterClass);
		return sb.toString();
	}

}
