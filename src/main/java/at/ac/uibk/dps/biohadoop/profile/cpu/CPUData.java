package at.ac.uibk.dps.biohadoop.profile.cpu;

public class CPUData {
	private final double processLoad;
	private final double systemLoad;

	public CPUData(double processLoad, double systemLoad) {
		this.processLoad = processLoad;
		this.systemLoad = systemLoad;
	}

	public double getProcessLoad() {
		return processLoad;
	}

	public double getSystemLoad() {
		return systemLoad;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("processLoad=").append(processLoad).append("|systemLoad=")
				.append(systemLoad);
		return sb.toString();
	}
}
