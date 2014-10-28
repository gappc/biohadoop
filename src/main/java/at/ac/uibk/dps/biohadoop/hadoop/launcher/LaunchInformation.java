package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.Adapter;

public class LaunchInformation {

	private final Adapter adapter;
	private final String pipelineName;

	public LaunchInformation(Adapter adapter, String pipelineName) {
		this.adapter = adapter;
		this.pipelineName = pipelineName;
	}

	public Adapter getAdapter() {
		return adapter;
	}

	public String getPipelineName() {
		return pipelineName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(", adapter=")
				.append(adapter.getClass().getCanonicalName())
				.append(", pipelineName=").append(pipelineName);
		return sb.toString();
	}

}
