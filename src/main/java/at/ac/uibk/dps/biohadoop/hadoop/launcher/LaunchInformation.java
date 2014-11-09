package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.Adapter;

public class LaunchInformation {

	private final Adapter adapter;

	public LaunchInformation(Adapter adapter) {
		this.adapter = adapter;
	}

	public Adapter getAdapter() {
		return adapter;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(", adapter=")
				.append(adapter.getClass().getCanonicalName());
		return sb.toString();
	}

}
