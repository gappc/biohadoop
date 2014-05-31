package at.ac.uibk.dps.biohadoop.applicationmanager;

public class Application {

	private String name;
	private float progress;
	private ApplicationState applicationState = ApplicationState.NEW;

	public Application(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
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
}
