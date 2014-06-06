package at.ac.uibk.dps.biohadoop.applicationmanager;


public class Application {

	private final ApplicationConfiguration applicationConfig;
	private float progress;
	private ApplicationState applicationState = ApplicationState.NEW;
	private ApplicationData<?> applicationData;

	public Application(ApplicationConfiguration applicationConfig) {
		this.applicationConfig = applicationConfig;
	}
	
	public ApplicationConfiguration getApplicationConfiguration() {
		return applicationConfig;
	}
	
	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public ApplicationState getApplicationState() {
		return applicationState;
	}

	public void setApplicationState(ApplicationState applicationState) {
		this.applicationState = applicationState;
	}

	public ApplicationData<?> getApplicationData() {
		return applicationData;
	}

	public <T>void setApplicationData(ApplicationData<T> applicationData) {
		this.applicationData = applicationData;
	}

}
