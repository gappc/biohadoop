package at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class AdapterConfiguration {

	private final Class<? extends Adapter> adapter;
	private String pipelineName;

	public AdapterConfiguration(Class<? extends Adapter> adapter,
			String pipelineName) {
		this.adapter = adapter;
		this.pipelineName = pipelineName;
	}

	@JsonCreator
	public static AdapterConfiguration create(
			@JsonProperty("adapter") Class<? extends Adapter> adapter,
			@JsonProperty("pipelineName") String pipelineName) {
		return new AdapterConfiguration(adapter, pipelineName);
	}

	public Class<? extends Adapter> getAdapter() {
		return adapter;
	}

	public String getPipelineName() {
		return pipelineName;
	}

	public void setPipelineName(String pipelineName) {
		this.pipelineName = pipelineName;
	}

	@Override
	public String toString() {
		String adapterClass = adapter != null ? adapter.getCanonicalName() : null;

		StringBuilder sb = new StringBuilder();
		sb.append("adapter=").append(adapterClass);
		sb.append(" pipelineName=").append(pipelineName);
		return sb.toString();
	}

}
