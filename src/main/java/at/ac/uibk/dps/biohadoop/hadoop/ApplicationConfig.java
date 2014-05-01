package at.ac.uibk.dps.biohadoop.hadoop;

public class ApplicationConfig {

	private String applicationMaster;

	public ApplicationConfig() {
	}

	public ApplicationConfig(String applicationMaster) {
		super();
		this.applicationMaster = applicationMaster;
	}

	public String getApplicationMaster() {
		return applicationMaster;
	}

	public void setApplicationMaster(String applicationMaster) {
		this.applicationMaster = applicationMaster;
	}
}
